es6升级es7时，es7并没有自动创建/更新索引的功能，于是自己实现了一下。
按照spring-data-elasticsearch语法写的，更改导入的包即可。

启动类上添加es实体类扫描路径
@EsScan({"com.zsw.entity.es","com.zsw.es.company"})

如果@PostConstruct等冲突，可以加上@DependsOn("esIndexCheck")保证索引先执行