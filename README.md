![](http://static.creatordev.io/logo.png)

----

# About the application

 The 'Android-Sesame' is a mobile Android application that lets the user to control
 garage door.
 
 Application is a part of bigger system that consist of 3 components:
 
  - [sesame-gateway](https://github.com/CreatorDev/Ci40-sesame): application running on Ci40,
  - [sesame-webapp](https://github.com/CreatorDev/webapp-sesame) : Node.js application managing door activity
  - [sesame-mobile](https://github.com/CreatorDev/android-sesame) : this android application 
 
 
 Main responsibility of the application is to provides a basic UI to control 
 the door.
 Additionally user can check web application logs or gathered statistics.
 
 Application can be easily extended as it is interacting only with web application via it's 
 [REST API](https://github.com/CreatorDev/webapp-sesame#rest-endpoints).
  
  
# Authorization
 This application uses token based authorization model.
 Token is generated at first launch - user have to provide 'web application' url
 and the 'secret' on the basis of which token will be calculated.
 From that point all requests will be decorated with 'x-access-token' header. 
 
 For more information please check [web application](https://github.com/CreatorDev/webapp-sesame) documentation.
 
 
 
 