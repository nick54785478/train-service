<h3>緣起</h3>
<hr />

&emsp;&emsp;於待業期間，想要弄個簡單作品集，所以有這個範例的誕生，改寫自前單位的新人訓練教材，一個基於六角形架構的火車與車票相關的微服務範例(原範例為 SpringBoot 2.X.X 、 DB 使用 MYSQL、 MQ 使用 RocketMQ)，
現改為 SpringBoot 3.3.3 並將 Rocket MQ 改為 Rabbit MQ，原因是一方面舊有屬於 Rocket MQ 的套件在 SpringBoot 3.X.X 無法使用，另一方面還可以順便研究與練習兔子的實作，但由於底層的 Code 我無法取得，所以只能根據自己的需求改寫出來，故與原先的火車範例有非常大的不同。
<br/>
此外，原範例並沒有會員相關的功能，但我覺得可以擴充此模組，所以也試著擴充此相關功能，順便能發想並新增一些額外其他的功能，如:訂票、檢核車票、退票等。
(註.會員系統是另外寫成 docker-compose，若您想要嘗試，需根據以下指引去實作，才能進行實作)
<br/>
其餘還有待擴充...


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

**註.** 
>* **auth-service 占用 8088 port，所以須在 application.properties 中設置 server.port=8088，再執行以下打包動作** 
>* **MySQL 占用 3307 port**

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

**範例:** <br/>

**POST**  http://localhost:8088/api/v1/users/register  <br/>

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

&emsp;透過 Docker 或 本地安裝，安裝 MYSQL 資料庫，以下為 yml 檔供參考:
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

<br/>


<h3>第三步: 建立表及相關外部依賴</h3>

>* **準備 MySQL (不論是 容器 或 本地端資料庫)。**
>* 執行專案目錄下的 init-schema.sql 進行。
>* 可執行 data.sql (我的測試資料)。
>* **準備 RabbitMQ (不論是 容器 或 本地端)。**

<br />


<h3>第四步: 使用 Postman 對其進行測試</h3>

train-demo.postman_collection.json
<br />

放在專案目錄下。





