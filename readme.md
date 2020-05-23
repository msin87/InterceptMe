#### Основной принцип работы перехватчика.

**Инжекция** вызова статичного метода `send` в метод `newCall` библиотеки `okhttp3` , а именно класса `okhttp3.OkHttpClient` ,путем декомпиляции **apk** файла приложения-цели и правки его **Dalvik** байт-кода (**.smali** файлов).  

#### Подготовка.

Перехватчик необходимо пересобрать в Android Studio под себя. В статичном методе `send` класса `InterceptMe` необходимо указать `botKey` вашего бота, `chatId`  чата куда подключен ваш бот (куда будут пересылаться запросы), и при необходимости поправить параметры `Proxy`. 

```java
public static void send(Request request) {
    try {
        //!!!Please modify this vars!!!
        int chatId = 0;
        String botKey = null;
        if (chatId==0 || botKey==null)
            return;
        //enter your telegram bot key
        Telegram telegram = new Telegram(botKey);
        //24.05.2020 this proxy is ok
        telegram.setProxyParams(Proxy.Type.SOCKS, "orbtl.s5.opennetwork.cc", 999, "369389927", "ElzXFZlC");
```

Для инжекции перехватчика необходимо:

1. Скачать [BatchApkTool](http://bursoft-portable.blogspot.com/2018/10/batch-apktool-376-donate.html) . Эта утилита позволит вам декомпилировать apk файлы. 

2. Собрать перехватчик. В Android Studio собрать apk файл перехватчика (*Build -> Build Bundle(s)/APK(s)->Buld APK(s)*)

3. Переместить собранный файл перехватчика в папку `BatchApkTool\_INPUT_APK` 

4. Переместить apk файл приложения-цели в эту же папку (`BatchApkTool\_INPUT_APK` )

5. Запустить `BatchApkTool.exe` и выбрать файл APK (на клавиатуре набрать 81 и Enter) перехватчика

6. Декомпилировать перехватчик (набрать 1 и Enter)

7. Выбрать apk файл приложения-цели и декомпилировать его (см. 5-6 пункты)

8. Открыть папку `BatchApkTool\_INPUT_APK`, найти в ней папку с декомпилированным перехватчиком

9. Найти файлы :

   - `InterceptMe.smali`
   - `NetworkTask$1.smali`
   - `NetworkTask.smali`
   - `Telegram.smali`

   Скорее всего они будут лежать примерно по такому пути: `BatchApkTool\_INPUT_APK\app-debug\smali_classes2\com\heavy87\InterceptMe`

10. Скопировать целиком папку `com` вместе с ее подпапками `\heavy87\InterceptMe` в декомпилированное приложение (тоже в папке `BatchApkTool\_INPUT_APK`) в самую последнюю папку smali. В декомпилированном приложении может быть несколько папок smali (например smali,smali_classes2,smali_classes3), это связано с тем, что есть [ограничение](https://developer.android.com/studio/build/multidex) на количество функций в dex-файлах (в декомплированном виде это перечисленные выше папки). Соответственно, если вы положите файлы перехватчика в "не последнюю" папку, то есть шанс, что при компиляции приложения вы получите ошибку. Для примера, структура приложения должна быть такой: 

    - assets
    - build
    - kotlin
    - lib
    - META-INF
    - ....
    - smali
    - smali_classes2
    - smali_classes3
    - smali_classes4
      - ...
      - com
        - ...
        - heavy87
          - ...
          - InterceptMe
            - `InterceptMe.smali`
            - `NetworkTask$1.smali`
            - `NetworkTask.smali`
            - `Telegram.smali`

11. Найти библиотеку okhttp3. Это будет папка внутри smali папок. 

12. Найти файл `OkHttpClient.smali` и открыть его редактором (например Sublime Text)

13. Найти в нем строку с методом:

    ```smali
    .method public newCall(Lokhttp3/Request;)Lokhttp3/Call;
    ```

    Это и будет метод, куда мы будем инжектировать перехватчик

14. Изменить реализацию метода на такую:

    ```
    .method public newCall(Lokhttp3/Request;)Lokhttp3/Call;
        .locals 1
    
        const/4 v0, 0x0
    
        invoke-static {p1}, Lcom/heavy87/InterceptMe/InterceptMe;->send(Lokhttp3/Request;)V
    
        invoke-static {p0, p1, v0}, Lokhttp3/RealCall;->newRealCall(Lokhttp3/OkHttpClient;Lokhttp3/Request;Z)Lokhttp3/RealCall;
    
        move-result-object p1
    
        return-object p1
    .end method
    ```

    То есть просто нужно добавить строку:

    ```
    invoke-static {p1}, Lcom/heavy87/InterceptMe/InterceptMe;->send(Lokhttp3/Request;)V
    ```

    Строку добавить нужно именно в указанном месте.

    (Не забудьте сохранить файл!)

15. Пересоберите приложение-цель. Выберите в BatchApkTool apk файл вашего приложения (81 Enter), и рекомпилируйюте apk (3 Enter)

16. Установите приложение рекомпилированный apk файл из папки `BatchApkTool\_OUT_APK` на телефон. 

    #### Внимание!

    Помните, что нужно в Telegram создать своего бота через бота [t.me/botFather](http://t.me/BotFather) , и добавить его в созданный вами чат. Узнать chatId можно запросив обновления у бота через его API: https://stackoverflow.com/questions/32423837/telegram-bot-how-to-get-a-group-chat-id 

    Если после правки `OkHttpClient.smali` у вас не собирается приложение, то переместите его в самую последнюю smali папку по тому же внутреннему пути, то есть создав внутри папку `okhttp3` . Прочтите пункт 10, и вы поймете почему надо так.

    **Используйте данный перехватчик только в личных целях. Пожалуйста, не вредите людям и компаниям. Все лежит на вашей совести. Я снимаю с себя ответственность за использование этого перехватчика. Трижды подумайте перед тем, как им пользоваться.**

    **Помните, что в некоторых случаях изменение кода приложения может преследоваться законом!**

    

    

