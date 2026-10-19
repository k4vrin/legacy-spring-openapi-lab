---
type: linkedin-post
language: fa
status: draft-unpublished-on-linkedin
created: 2026-10-15
updated: 2026-10-19
format: caption-and-carousel
---

# پیش‌نویس پست لینکدین

این متن هنوز در لینکدین منتشر نشده. مثال‌ها مربوط به نمونه مستقل `legacy-spring-openapi-lab` هستن و کدهای زیر با پیاده‌سازی همین ریپو مطابقت دارن.

## کپشن

وقتی Spring خودش Component Scan داره، چرا باید یه Configuration رو صریح توی XML ثبت کنیم؟

برای بررسی این سؤال، ما یه نمونه مستقل با API فرضی سفارش‌ها ساختیم: Spring MVC با تنظیمات XML، خروجی WAR روی Tomcat و بدون Spring Boot startup.

می‌خواستیم Springdoc رو اضافه کنیم، بدون اینکه محدوده کشف Beanها رو بی‌دلیل بزرگ کنیم. برای همین اسکن رو به پکیج API و Annotationهای Controller و ControllerAdvice محدود کردیم و Configuration مستندات رو بیرون از این محدوده، با یه `<bean>` توی XML ثبت کردیم.

`@Configuration` به‌تنهایی باعث کشف کلاس نمی‌شه. کلاس باید از طریق Scan، Import یا ثبت صریح وارد پردازش Spring بشه. توی نمونه ما هم پکیج `docs` بیرون از اسکنه، هم فیلترها فقط Controller و Advice رو می‌پذیرن.

ما مرز Contextها رو هم حفظ کردیم: سرویس و Repository توی Root هستن؛ Controllerها، MVC و OpenAPI توی Child متعلق به DispatcherServlet. Child می‌تونه Beanهای Parent رو پیدا کنه، ولی Parent به Beanهای مختص Child دسترسی نداره.

فعال شدن مستندات همچنان با فایل properties و JVM system property کنترل می‌شه. Initializer قبل از refresh فایل رو به Environment اضافه می‌کنه؛ بعد Condition مقدار مؤثر رو می‌خونه و درباره ثبت Configuration تصمیم می‌گیره. ما I/O رو داخل شرط نبردیم و دیتابیسی هم اضافه نکردیم.

این تصمیم موقع startup گرفته می‌شه. تغییر مقدار بعد از بالا اومدن برنامه، به‌تنهایی Beanها رو ایجاد یا حذف نمی‌کنه؛ برای اعمال این تصمیم باید Context دوباره ساخته بشه.

تست‌ها کشف محدود Controllerها، ثبت صریح Configuration، مرز Root/Child و هر دو حالت flag رو بررسی می‌کنن. وقتی مستندات خاموشه، endpointهای JSON، YAML و فایل‌های Swagger UI در دسترس نیستن، ولی API سفارش‌ها و خطاهای JSON خودشون کار می‌کنن. تست WAR همین مسیرها رو روی Tomcat هم بررسی می‌کنه.

کد و راهنمای معماری:
https://github.com/k4vrin/legacy-spring-openapi-lab

شما برای اضافه کردن Configuration به یه پروژه Legacy، چه زمانی ثبت صریح رو به گسترش Scan ترجیح می‌دید؟

#Java #SpringMVC #SpringFramework #OpenAPI #BackendDevelopment

## کاروسل

### اسلاید ۱ — چرا Spring یه Configuration رو پیدا نمی‌کنه؟

وقتی Component Scan داریم، چرا گاهی Bean رو صریح توی XML ثبت می‌کنیم؟

`Legacy Spring MVC • OpenAPI`

### اسلاید ۲ — مسئله ما

ما می‌خواستیم OpenAPI رو به یه نمونه Spring MVC با XML اضافه کنیم. خروجی همچنان WAR روی Tomcat می‌مونه؛ قرارداد APIها و startup برنامه هم حفظ می‌شن.

Spring Boot startup نداریم، ولی Springdoc از کتابخانه‌های پشتیبان Boot استفاده می‌کنه؛ پس نمی‌گیم هیچ کتابخانه Boot توی dependencyها نیست.

### اسلاید ۳ — web.xml چه کار می‌کنه؟

Tomcat از روی `web.xml` Listener و Servlet رو راه می‌اندازه و درخواست رو به Servlet مربوط می‌رسونه:

```xml
<servlet-mapping><servlet-name>orders</servlet-name><url-pattern>/api/*</url-pattern></servlet-mapping>
```

`HTTP → Tomcat → DispatcherServlet → Spring MVC Controller`

`orders-servlet.xml` تنظیمات Beanهای MVC رو مشخص می‌کنه. Servlet mapping مسیر ورود درخواست رو تعیین می‌کنه؛ component scanning کلاس‌های واجد شرایط رو کشف می‌کنه. این دو کار یکی نیستن.

### اسلاید ۴ — دو Context

`ContextLoaderListener → root-context.xml → OrderService + InMemoryOrderRepository`

`DispatcherServlet → orders-servlet.xml → Controller + Advice + MVC + OpenAPI`

Child سرویس‌ها رو از Parent پیدا می‌کنه. Parent به Controllerهای مختص Child دسترسی نداره. ما سرویس‌ها رو دوباره توی Child ثبت نمی‌کنیم و همون یک تنظیم MVC و `mvcConversionService` مشترک رو نگه می‌داریم.

### اسلاید ۵ — هم پکیج مهمه، هم Filter

```xml
<context:component-scan base-package="dev.kavrin.legacyorders.api" use-default-filters="false">
  <context:include-filter type="annotation" expression="org.springframework.stereotype.Controller"/>
  <context:include-filter type="annotation" expression="org.springframework.web.bind.annotation.ControllerAdvice"/>
</context:component-scan>
```

فیلتر اول `@RestController` رو هم از طریق meta-annotation می‌پذیره. فیلتر دوم `@RestControllerAdvice` موجود رو نگه می‌داره تا خطاهای JSON قبلی حفظ بشن. ما Advice جدیدی اضافه نکردیم؛ Componentها و Configurationهای معمولی توی این پکیج هم خودکار ثبت نمی‌شن.

### اسلاید ۶ — چرا @Configuration کافی نیست؟

کلاس `dev.kavrin.legacyorders.docs.ApiDocsConfiguration` بیرون از پکیج `dev.kavrin.legacyorders.api` قرار داره.

حتی اگه فقط پکیج Scan رو گسترده کنیم، یه `@Configuration` معمولی با فیلترهای Controller و Advice مطابقت نداره. Annotation کلاس رو برای پردازش معرفی می‌کنه؛ خودش کلاس رو وارد Context نمی‌کنه.

### اسلاید ۷ — تصمیم ما: ثبت صریح

ما به‌جای گسترش پکیج و فیلترهای اسکن، همین Configuration رو توی `orders-servlet.xml` معرفی کردیم:

```xml
<bean id="apiDocsConfiguration" class="dev.kavrin.legacyorders.docs.ApiDocsConfiguration"/>
```

این انتخاب ورود Beanهای جدید رو قابل‌ردگیری نگه می‌داره و احتمال کشف ناخواسته سرویس‌ها یا زیرساخت‌های دیگه رو کم می‌کنه. این یه انتخاب متناسب با مرزهای این نمونه است؛ گسترش Scan همیشه تصمیم اشتباهی نیست.

### اسلاید ۸ — ثبت شرطی و تولید Spec

کلاس ثبت‌شده همچنان این Annotationها رو داره:

```java
@Configuration(proxyBeanMethods = false)
@Conditional(ApiDocsEnabledCondition.class)
@Import({SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class})
```

`Initializer → Environment → @Conditional → Configuration + imports + beans`

پیش‌فرض فایل classpath:

```properties
demo.api-docs.enabled=false
```

Initializer با `addLast` پیش‌فرض‌ها رو اضافه می‌کنه تا JVM system property اولویت داشته باشه. پردازش Annotationها از طریق component-scan فعاله؛ ثبت XML شرط رو دور نمی‌زنه. با false، Importها و متدهای Bean پردازش نمی‌شن و Beanهای مستندات ساخته نمی‌شن؛ خود شیء Configuration که XML ثبت کرده توی Context می‌مونه، ولی مستنداتی اضافه نمی‌کنه. مقدار نامعتبر هم startup رو متوقف می‌کنه.

بعد از ثبت، Springdoc متادیتای Mappingها، DTOها و Validation رو بررسی می‌کنه؛ برای ساخت Spec لازم نیست منطق ایجاد سفارش اجرا بشه. فیلترهای Springdoc یعنی `packagesToScan` و `pathsToMatch` فقط محتوای Spec رو محدود می‌کنن؛ Controller ثبت نمی‌کنن و مسیر HTTP رو تغییر نمی‌دن.

### اسلاید ۹ — چی رو بررسی می‌کنیم؟

ما هر دو حالت روشن و خاموش رو با تست Context و اجرای WAR روی Tomcat بررسی می‌کنیم:

- Controller و Advice از اسکن محدود میان؛ Configuration مستندات از XML.
- Child سرویس Parent رو می‌بینه؛ Root نمی‌تونه Controllerهای Child رو پیدا کنه.
- مستندات در حالت خاموش 404 می‌دن؛ API و خطاهای سفارش‌ها در هر دو حالت حفظ می‌شن.
- تغییر flag بعد از startup ثبت Beanها رو تغییر نمی‌ده.

در بررسی ۱۹ اکتبر ۲۰۲۶، هر ده مورد تست پاس شدن: هشت مورد Context/MockMvc و دو مورد اجرای WAR. نیازی به دیتابیس نداریم. نتیجه‌های اجرایی و محدودیت‌ها توی [گزارش بررسی](VERIFICATION.md) ثبت می‌شن؛ این بررسی‌ها ادعای تأیید محیط کاری یا deployment خارجی نیستن.

https://github.com/k4vrin/legacy-spring-openapi-lab
