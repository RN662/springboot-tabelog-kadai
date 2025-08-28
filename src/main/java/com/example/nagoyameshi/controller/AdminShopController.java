package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.ShopHoliday;
import com.example.nagoyameshi.form.ShopEditForm;
import com.example.nagoyameshi.form.ShopRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopHolidayRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.service.ShopService;

@Controller
@RequestMapping("/admin/shops")
public class AdminShopController {
	private final ShopRepository shopRepository;
	private final CategoryRepository categoryRepository;
	private final ShopService shopService;
	private final ShopHolidayRepository shopHolidayRepository;

	public AdminShopController(ShopRepository shopRepository, CategoryRepository categoryRepository,
			ShopService shopService, ShopHolidayRepository shopHolidayRepository) {
		this.shopRepository = shopRepository;
		this.categoryRepository = categoryRepository;
		this.shopService = shopService;
		this.shopHolidayRepository = shopHolidayRepository;
	}

	@GetMapping
	public String index(Model model,
			@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.ASC) Pageable pageable,
			@RequestParam(name = "keyword", required = false) String keyword) {
		Page<Shop> shopPage;

		if (keyword != null && !keyword.isEmpty()) {
			shopPage = shopRepository.findByNameLike("%" + keyword + "%", pageable);
		} else {
			shopPage = shopRepository.findAll(pageable);
		}

		model.addAttribute("shopPage", shopPage);
		model.addAttribute("keyword", keyword);

		return "admin/shops/index";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model) {
		Shop shop = shopRepository.getReferenceById(id);

		ShopHoliday holiday = shopHolidayRepository.findByShop(shop);

		model.addAttribute("shop", shop);
		model.addAttribute("holiday", holiday);

		return "admin/shops/show";
	}

	@GetMapping("/register")
	public String register(Model model) {
		List<Category> categories = categoryRepository.findAll();

		model.addAttribute("shopRegisterForm", new ShopRegisterForm());
		model.addAttribute("categories", categories);

		return "admin/shops/register";
	}

	@PostMapping("/create")
	public String create(@ModelAttribute @Validated ShopRegisterForm shopRegisterForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model) {
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryRepository.findAll();
			model.addAttribute("categories", categories);

			return "admin/shops/register";
		}

		Category category = categoryRepository.getReferenceById(shopRegisterForm.getCategoryId());

		shopService.create(category, shopRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");

		return "redirect:/admin/shops";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id, Model model) {
		Shop shop = shopRepository.getReferenceById(id);
		String imageName = shop.getImageName();
		List<Category> categories = categoryRepository.findAll();
		
		// サービスに移動
		ShopEditForm shopEditForm = shopService.toShopEditForm(shop);
		
		model.addAttribute("imageName", imageName);
		model.addAttribute("shopEditForm", shopEditForm);
		model.addAttribute("categories", categories);

		return "admin/shops/edit";
	}

	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated ShopEditForm shopEditForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model) {
		if (bindingResult.hasErrors()) {
			Shop shop = shopRepository.getReferenceById(shopEditForm.getId());
			String imageName = shop.getImageName();

			List<Category> categories = categoryRepository.findAll();

			model.addAttribute("imageName", imageName);
			model.addAttribute("categories", categories);

			return "admin/shops/edit";
		}

		Category category = categoryRepository.getReferenceById(shopEditForm.getCategoryId());

		shopService.update(category, shopEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗情報を編集しました。");

		return "redirect:/admin/shops";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		try {
			shopService.delete(id);
			redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました。");
		}
			
		return "redirect:/admin/shops";
	}
}
