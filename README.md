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
		     
<h3>第一步: Clone reactive-system-demo </h3>
https://github.com/nick54785478/reactive-system-demo

<br/>
1. 在Maven項目或者pom.xml上右鍵 -->  Run As --> "Maven Build... " 或 Run Configuration --> "Maven Build"。 <br/>
2. 在"Goals"输入框中输入：**clean install** 。 <br/>
3. 使用時在 Run As 中選中 Maven build 即可。 <br/>
4. console 出現 BUILD SUCCESS 即打包完成。 <br/>
5. 透過命令列(CMD) 進入 Dockerfile 所在目錄(我放在專案內那一層)。 <br/>
6. 輸入 **docker build -t {image 名稱} .**  (註. 可透過 docker images 看是否有打包成功。)。 <br/>
7. 解壓縮 **auth-service.zip** (放在專案內那一層)。 <br/>
8. 到 auth-service 資料夾內點擊 run.sh，即容器化完成。 <br/>
9. 在該 MySQL 中建立 auth 庫，並將 schema.sql 和 data.sql 的內容執行(新增資料表及資料)。 <br/>
10. 註冊一個使用者帳號以進行後續動作，因需要通過 JWToken 驗證，且相關功能可能會用使用者帳號。 <br/>
<br/> 
**範例:**  <br/>
**POST**  http://localhost:8088/api/v1/users/register <br/>
Request Body :
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

<h3>第二步: Clone train-demo</h3>

透過 Docker 或 本地安裝，安裝 MYSQL 資料庫，以下為 yml 檔供參考:
```
version: "3"
services:
  db:
    image: mysql:8.0
    container_name: local-mysql
    restart: always
    environment:
      TZ: Asia/Taipei
      MYSQL_ROOT_PASSWORD: root 
    command:
      --max_connections=1000
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password
    ports:
      - 3306:3306
    volumes:
      - ./data:/var/lib/mysql
      - ./conf:/etc/mysql/conf.d
    networks:
        mysql:
          aliases:
            - mysql
networks:
  mysql:
    name: mysql
    driver: bridge
```
<br/>


**可執行下列指令建立 docker container**

```
        docker-compose up -d
``` 


<h3>第三步: 建立表及新增相關資料</h3>

執行專案目錄下的 init-schema.sql 進行

<br />


<h3>第四步: 使用Postman 對其進行測試</h3>



