package com.example.demo.base.config.security;

public enum JwtConstants {

	 /**
     * HTTP 請求頭中用於攜帶 JWT 的名稱。
     */
    JWT_HEADER("Authorization"),
    
    /**
     * JWT 的前綴，用於標識 JWT 的類型。
     */
    JWT_PREFIX("Bearer "),
    
    /**
     * JWT 中用於標識主體的索引鍵。
     */
    JWT_CLAIM_KEY_SUB("sub"),
	
	/**
	 * JWT 在程式碼中統一引用 JWT 中角色資訊的欄位名稱
	 * */
	JWT_CLAIMS_KEY_ROLE("roles"),
	
	/**
	 * JWT 在程式碼中統一引用 JWT 中使用者帳號
	 * */
	JWT_CLAIMS_KEY_USER("username"), 
	
	/**
	 * JWT 在程式碼中統一引用 JWT 中使用者信箱
	 * */
	JWT_CLAIMS_KEY_EMAIL("email");
	
    private String value;

    /**
     * 建構函數，設定對應的常數值。
     * @param value 常數的值
     */
    JwtConstants(String value) {
        this.value = value;
    }

    /**
     * 取得常數的值。
     * @return 常數的值
     */
    public String getValue() {
        return value;
    }
}
