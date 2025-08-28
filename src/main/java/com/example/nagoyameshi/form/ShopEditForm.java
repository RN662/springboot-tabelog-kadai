package com.example.nagoyameshi.form;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShopEditForm {
	@NotNull
	private Integer id;
	
	@NotNull(message = "カテゴリを選択してください。")
	private Integer categoryId;
	
	@NotBlank(message = "店舗名を入力してください。")
	private String name;
	
	// 画像
	private MultipartFile imageFile;
	
	@NotBlank(message = "説明を入力してください。")
	@Length(max = 500, message = "説明文は500文字以内で入力してください。")
	private String description;
	
	@NotNull(message = "最低価格を入力してください。")
	@Min(value = 1, message = "最低価格は1円以上に設定してください。")
	private Integer lowestPrice;
	
	@NotNull(message = "最高価格を入力してください。")
	@Min(value = 1, message = "最高価格は1円以上に設定してください。")
	private Integer highestPrice;
	
	@NotBlank(message = "開店時間を入力してください。")
	private String openingTime;
	
	@NotBlank(message = "閉店時間を入力してください。")
	private String closingTime;
	
	// 定休日（バリデーションチェックなし）
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
	
	@NotBlank(message = "郵便番号を入力してください。")
	@Pattern(regexp = "^[0-9]{3}-[0-9]{4}$", message = "郵便番号は半角で 123-4567 の形式で入力してください。")
	private String postalCode;
	
	@NotBlank(message = "住所を入力してください。")
	private String address;
	
	@NotBlank(message = "電話番号を入力してください。")
	@Pattern(regexp = "^[0-9]{2,4}-[0-9]{2,4}-[0-9]{3,4}$", message = "電話番号は半角ハイフン区切り（例 052-250-1001）で入力してください。")
	private String phoneNumber;
	
	@NotNull(message = "座席数を入力してください。")
	@Min(value = 1, message = "座席数は1席以上に設定してください。")
	private Integer seatingCapacity;

}
