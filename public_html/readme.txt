1. Compile the java code in WEB-INF/classes: javac cs5530/*.java

2. Edit the web.xml in WEB-INF; replace Your Name with your first name
and last name.

3. Access your homepage via http://georgia.eng.utah.edu:8080/~cs5530uxx

4. To work on your own database, you need to modify the credentials in Connector.java (so that it connects to your own database, rather than the class-wide database cs5530db). Note that the sample JSP code then will not work, since the sample "orders" table does not exist in your own database. You can always use the distributed script orders.sql (use "source orders.sql;" in mysql client) to produce that table in your own database, so that the sample JSP code will work with your own database. (be careful when doing this if your database already has a "orders" table from your phase 2; that table will be overwritten. To avoid that, change the orders.sql script to have a different table name, and also change the Orders.java code to query the new table instead).
