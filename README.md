# Time-Tracker
## Task
Система Time-Tracking. Администратор закрепляет за пользователем
Активность. У пользователя может быть одна или несколько Активностей.
Пользователь отмечает кол-во затраченного времени на каждую активность.
Пользователь может отправить запрос на добавление/удаление
Активности.
### Installation
Clone project from this repository `git clone https://github.com/Yurwar/time-tracker.git`

Go to **../src/main/resources/database.properties** and change properties on yours

Execute script from 
**../src/main/resources/time-tracker-dump.sql** to recover database from dump

Go to project root folder and execute in terminal: `mvn clean install`

After that head to **../target** and copy *time-tracker.war* in **$TOMCAT_HOME/webapps** folder
### Run application
Go to **$TOMCAT_HOME/bin** folder

To start server execute following script `startup.sh` 
or `startup.bat` depending on the installed operating system

After server started successfully, 
you can access application at *http://localhost:8080/time-tracker/app*

On login page fill username: _admin_ and password: _admin_, then you can sign up with new user or change current

To stop server execute following script `shutdown.sh` 
or `shutdown.bat` depending on the installed operating system
Hello
