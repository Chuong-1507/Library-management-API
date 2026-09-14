package com.example.chuong.librarymanagementapi.specification;

import com.example.chuong.librarymanagementapi.dto.request.BookFilterRequest;
import com.example.chuong.librarymanagementapi.entity.Book;
import jakarta.persistence.criteria.JoinType;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Định nghĩa các điều kiện Specification<Book> lọc động.
 * Mỗi method trả về 1 Specification độc lập, có thể kết hợp bằng .and().
 */
@UtilityClass
public class BookSpecification {
    //Điều kiện title
    public static Specification<Book> hasTitle(String title){
        return (root, query, cb) ->{
            if (!StringUtils.hasText(title)){// Kiểm tra title có rỗng không
                return cb.conjunction(); //trả TRUE luôn đúng -> bỏ qua điều kiện này
            }
            return cb.like(cb.lower(root.get("title")),"%" + title.toLowerCase() + "%");//kiểm tra điêu kiện có chứa cụm giá trị title ko ?
        };
    }
    //Điều kiện author
    public static Specification<Book> hasAuthor (String author){
        return (root, query, cb) -> {
            if (!StringUtils.hasText(author)){
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
        };
    }
    //Điều kiện Category
    public static Specification<Book> hasCategory(UUID categoryId){
        return ((root, query, criteriaBuilder) -> {
            if (categoryId == null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("id"),categoryId);//Kiểm tra điều kiện có đúng với categoryId không?
        });
    }
    //Điều kiện price
    public static Specification<Book> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice){
        return ((root, query, criteriaBuilder) -> {
            if (minPrice != null && maxPrice != null){
                return criteriaBuilder.between(root.get("price"),minPrice,maxPrice);
            }
            if (minPrice != null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"),minPrice);
            }
            if (maxPrice != null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"),maxPrice);
            }
            return criteriaBuilder.conjunction();
        });
    }
    //Điều kiện publicationYear
    public static Specification<Book> hasPublicationYear(Integer publicationYear){
        return ((root, query, criteriaBuilder) -> {
            if (publicationYear == null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("publicationYear"),publicationYear);
        });
    }

    /**
     * Kết hợp toàn bộ điều kiện từ BookFilterRequest thành 1 Specification duy nhất.
     * Field nào null/rỗng trong filter sẽ tự động bị bỏ qua (nhờ cb.conjunction() ở trên).
     *
     * Đồng thời JOIN FETCH category ngay trong query chính để tránh N+1 query
     * khi BookMapper map categoryName cho từng item trong danh sách kết quả.
     */
    public static Specification<Book> withFilter (BookFilterRequest filter){
        List<Specification<Book>> specs = new ArrayList<>();
        specs.add(hasTitle(filter.getTitle()));
        specs.add(hasAuthor(filter.getAuthor()));
        specs.add(hasCategory(filter.getCategoryId()));
        specs.add(hasPriceBetween(filter.getMinPrice(),filter.getMaxPrice()));
        specs.add(hasPublicationYear(filter.getPulicationYear()));

        Specification<Book> combined = Specification.where(fetchCategory());
        for (Specification<Book> spec : specs){
            combined = combined.and(spec);
        }
        return combined;
    }
    /**
     * Fetch join category để tránh N+1 query (Book -> Category là quan hệ LAZY).
     * Lưu ý: chỉ áp dụng fetch join cho query lấy dữ liệu (findAll với Pageable),
     * KHÔNG áp dụng cho query COUNT (Spring Data tự động bỏ fetch khi đếm),
     * nên không lo bị lỗi "firstResult/maxResults specified with collection fetch".
     */
    private static Specification<Book> fetchCategory(){
        return ((root, query, criteriaBuilder) -> {
            // Chỉ fetch khi đây không phải query đếm (count query trả về Long)
            if (query.getResultType() != Long.class && query.getResultType() != long.class){
                root.fetch("category", JoinType.LEFT); //lấy luôn Category cùng với Book bằng LEFT JOIN
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        });
    }
}
