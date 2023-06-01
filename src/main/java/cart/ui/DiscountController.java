package cart.ui;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cart.application.PriceService;
import cart.dto.TotalDiscountInfoResponse;

@RestController
@RequestMapping("/discount")
public class DiscountController {
    private final PriceService priceService;

    public DiscountController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    public ResponseEntity<TotalDiscountInfoResponse> computeAllDiscountInformation(
            @RequestParam("price") Integer price,
            @RequestParam("memberGrade") String grade
    ) {
        final TotalDiscountInfoResponse discountInformation = priceService.getDiscountInformation(price, grade);
        return ResponseEntity.ok(discountInformation);
    }
}
