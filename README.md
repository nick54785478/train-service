<h3>背景</h3>
<hr />

TODO


<br/>

<h3>框架及外部依賴</h3>

>* Java
>* SpringBoot 3.3.3
>* JDK 17
>* MySQL
>* Rabbit MQ

<br/>
		     


<h3>第一步: 安裝 reactive-system-demo </h3>
https://github.com/nick54785478/reactive-system-demo

<br/>

1. 在Maven項目或者pom.xml上右鍵 -->  Run As --> "Maven Build... " 或 Run Configuration --> "Maven Build"。
2. 在"Goals"输入框中输入：**clean install** 。
3. 使用時在 Run As 中選中 Maven build 即可。
4. console 出現 BUILD SUCCESS 即打包完成。
5. 透過命令列(CMD) 進入 Dockerfile 所在目錄(我放在專案內那一層)
6. 輸入 **docker build -t {image 名稱} .**  (註. 可透過 docker images 看是否有打包成功。)
7. 解壓縮 **auth-service.zip** (在專案內那一層)
8. 到裡面點擊 run.sh，即容器化完成。
9. 在該 MySQL 中建立 auth 庫，並將 init-schema.sql 的內容執行(新增資料表及資料)
10. 註冊一個使用者帳號以進行後續動作。
<br/> 

**範例:**
	**POST** http://localhost:8088/api/v1/users/register
<br/>	Request Body :
<br/>
 	```
	 {
	    "name": "Nick",
	    "email": "nick123@example.com", // 信箱
	    "username":"nick123@example.com", // 帳號與信箱同(以利後續使用)
	    "password":"password123", // 密碼
	    "address":"台北市內湖區"	
	}
 	```

<br/> 





<br/>



<br/>


**可執行下列指令建立 docker container**

```
        docker-compose up -d
```




<h3>第二步: 建立表及新增相關資料</h3>
相關表與資料如下:

```
	CREATE TABLE IF NOT EXISTS user_info (
	    `id` BIGINT(20) AUTO_INCREMENT,
	    `name` VARCHAR(100),
	    `username` VARCHAR(100),
	    `password` VARCHAR(100),
	    `address` VARCHAR(255),
	    `email` VARCHAR(100),
	    `active_flag` CHAR(1),
	    PRIMARY KEY (`id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
	
	CREATE TABLE IF NOT EXISTS role_info (
	    `id` BIGINT(20) AUTO_INCREMENT,
	    `name` VARCHAR(100),
	    `type` VARCHAR(100),
	    `description` VARCHAR(255),
	    `active_flag` CHAR(1),
	    PRIMARY KEY (`id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
	
	CREATE TABLE IF NOT EXISTS auth_info (
	    `id` BIGINT(20) AUTO_INCREMENT,
	    `user_id` BIGINT(20),
	    `role_id` BIGINT(20),
	    `active_flag` CHAR(1),    
	    PRIMARY KEY (`id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

	# system 密碼 system123
	INSERT INTO user_info (name, username, password, address, email, active_flag) VALUES ('System', 'system', '$2a$10$eKT3qdUVQO1mf0.hUZswDuZiO69BKv20OjE3lPITJYqQol4MYAWNm','', 'system@example.com', 'Y');	
	INSERT INTO role_info (name, type, description, active_flag) VALUES ('ADMIN', 'ROOT', '系統管理員', 'Y');
	INSERT INTO role_info (name, type, description, active_flag) VALUES ('DATA_OWNER', 'USER', '新增、修改、刪除、讀取所有使用者、角色資料權限', 'Y');	
	INSERT INTO auth_info (user_id, role_id, active_flag) VALUES (1, 1, 'Y');
```

<br />


<h3>第三步: 使用Postman 或 WebClient 對其進行測試</h3>

**註. 請先執行第二步，新增 Admin 角色資料，之後註冊新帳號，將DATA_OWNER 權限賦予給該帳號，開始執行後續。**

> * Postman 作法:
根據 iface.handler 中的 URL 去建立 Request (有些要記得設置 Token，透過 LoginHandler 內 /login 取得 JWToken )。
> * WebClient 作法:
可參考 ReactiveSystemDemoApplicationTests，裡面有示範。


