package gift.view;

import gift.global.annotation.OnlyForAdmin;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/products")
    public String products() {
        return  "all-product";
    }

    @GetMapping("/products/new")
    public String addProduct() {
        return "add-product";
    }

    @OnlyForAdmin
    @GetMapping("/admin/products/new")
    public String addAdminProduct() {
        return "add-admin-product";
    }

    @GetMapping("/wishlist")
    public String wishlist() {
        return "wishlist";
    }

    @GetMapping("/my/products")
    public String myProducts() {
        return "my-product";
    }

    @GetMapping("/my/products/{id}")
    public String editProduct(@PathVariable Long id, Model model){

        model.addAttribute("productId", id);

        return "edit-my-product";
    }

    @OnlyForAdmin
    @GetMapping("/admin/products")
    public String adminProducts(){
        return "admin-product";
    }

    @OnlyForAdmin
    @GetMapping("/admin/members")
    public String adminMembers(){
        return "admin-member";
    }

    @OnlyForAdmin
    @GetMapping("/admin/products/{id}")
    public String editProductForAdmin(@PathVariable Long id, Model model){

        model.addAttribute("productId", id);

        return "edit-admin-product";
    }

    @OnlyForAdmin
    @GetMapping("/admin/members/add")
    public String addMember(){
        return "add-member";
    }

    @OnlyForAdmin
    @GetMapping("/admin/members/{id}")
    public String editMember(@PathVariable Long id, Model model){
        model.addAttribute("memberId", id);

        return  "edit-member";
    }

    @GetMapping("/my/products/{productId}/options/{optionId}")
    public String editOptionQuantity(@PathVariable Long productId, @PathVariable Long optionId, Model model){
        model.addAttribute("optionId", optionId);
        model.addAttribute("productId", productId);

        return "edit-my-product-option";
    }

    @OnlyForAdmin
    @GetMapping("/admin/products/{productId}/options/{optionId}")
    public String editOptionForAdmin(@PathVariable Long productId, @PathVariable Long optionId, Model model){
        model.addAttribute("optionId", optionId);
        model.addAttribute("productId", productId);

        return "edit-admin-product-option";
    }

    @GetMapping("/my/products/{productId}/options")
    public String editOptionPage(@PathVariable Long productId, Model model){
        model.addAttribute("productId", productId);

        return "my-product-option";
    }

    @OnlyForAdmin
    @GetMapping("/admin/products/{productId}/options")
    public String editOptionPageForAdmin(@PathVariable Long productId, Model model){
        model.addAttribute("productId", productId);

        return "admin-product-option";
    }

    @GetMapping("/products/{productId}/select-option")
    public String selectOption(@PathVariable Long productId, Model model){
        model.addAttribute("productId", productId);
        return "select-option";
    }

}
