## A tool that make your apk debuggable for Charles/Fiddler

## config file in `config/config.properties`
- add apk path,out apk path
- add your sign file(or you can use test.jks)
- add your certFile that export from Charles/Fiddler。

## export cert file from Charles
open Charles,then `Help` -> `SSL Proxying` -> `Save Charles Root Certificate...` -> `change Format to Binary certificate(.cer)`-> `Save` 

## how to use

``` sh
java -jar ApkCrack.jar
```


