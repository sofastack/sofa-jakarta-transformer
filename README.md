<a name="iXhMX"></a>
## 简介
SOFA Jakarta Transformer是一个基于开源工具Eclipse Transformer的Java产物转换工具，目前主要用对Jar包从Java EE到Jakarta的转换。引用Eclipse Transformer的介绍：
> The Eclipse Transformer project is part of the Eclipse Technology top-level project.
>
> Eclipse Transformer provides tools and runtime components that transform Java binaries, such as individual class files and complete JARs and WARs, mapping changes to Java packages, type names, and related resource names.
>
> While the initial impetus for the project was the Jakarta EE package renaming issue, the scope of the project is broader to support other renaming scenarios. For example, shading.
>
> We operate under the Eclipse Code of Conduct to promote fairness, openness, and inclusion.

其本质上就是能够通过配置好的转换规则执行对java，class，xml等文件以及JAR，WAR，ZIP等压缩包执行文本转换的工具，我们主要将其用在Java EE到Jakarta EE的转换场景中。
<a name="YRGQj"></a>
## 背景
随着 Oracle 将 Java EE 交给 Eclipse 开始 Java EE 就成为了历史，取而代之的是新的 Jakarta EE 系列。对于开发者而言，最大的改变便是从 jakarta EE 9 开始， EE 相关的类的命名空间从 `javax.*` 迁移到了 `jakarta.*`。<br />对于这一变更，开源社区的组件也均在不同进度的进行升级适配：例如 Tomcat 从 10.x 版本开始便迁移到了 jakarta 命名空间。而 SpringBoot 3.x 版本也不再支持 Java EE 8，而是基于 Jakarta EE 9 构建，这使得我们基于 SpringBoot 3.x 开发的 SOFABoot 4.x 版本也将基于 Jakarta EE 9 构建，使用 javax 命名空间的组件需要进行 jakarta 命名空间的切换才能在 SOFABoot 4.x 环境使用。<br />因此，我们需要解决这一兼容性问题，使得这些 SDK 能够屏蔽 Java EE/Jakarta EE 的实现差异，低成本的同时支持两种命名空间。
<a name="fw8gv"></a>
## 能力
<a name="g1wrU"></a>
### 能对谁转换
能转换的对象主要分为两类：<br />单体类型：Class，Java，Jsp，XML，Manifest，Properties（-tf规则可以对任意类型文件都生效）<br />容器类型：包括目录，ZIP，JAR，WAR，RAR，EAR。会递归对下面包含的所有单体类型文件进行转换
<a name="pUnlk"></a>
### 能做什么转换
以下例子中的配置文件对于命令行，Maven插件和API均有效，命令则以命令行为例
<a name="e6QXH"></a>
#### Package重命名
**作用域以及作用方式：**

- Java文件，对所有包含指定Package名的文本替换为指定文本
- Manifest文件，MANIFEST.MF和featureName.mf文件中的包引用。
- Class文件，更新匹配到的class 文件中的类引用，class 文件中的 java 字符串常量中出现的类名，class 文件中的 java 字符串常量中出现的资源引用。
- XML文件，对所有包含指定Package名的文本替换为指定文本

命令参数：-tr,--renames <配置文件路径> <br />**例子：**
```bash
-tr /xxx/jakarta-rename.properties
```
jakarta-rename.properties（指定具体替换规则）
```properties
javax.batch.api.chunk.listener=jakarta.batch.api.chunk.listener
```
<a name="TsyAk"></a>
#### Package版本更新
**作用域以及作用方式：** Manifest文件，更新OSGi 属性"DynamicImport-Package", "Export-Package", "Import-Package", "Subsystem-Content", and "IBM-API-Package"中的指定package的版本。<br />**命令参数：**-tv,--versions <配置文件路径><br />**例子：**
```bash
-tv /xxx/jakarta-versions.properties
```
jakarta-versions.properties（指定每个package的版本范围）
```properties
jakarta.activation=[2.0,3)
jakarta.annotation.security=[2.0,3)
jakarta.annotation.sql=[2.0,3)
```
<a name="FB675"></a>
#### 字符串常量更新
**作用域以及作用方式：**<br />Class文件，对所有Class文件按照所给的映射做字符串常量替换（rename操作时也会对字符串常量做替换）<br />Java文件，同Class文件<br />**命令参数：**-td,--direct <配置文件路径> <br />**例子：**
```bash
-td /xxx/xxx/jakarta-direct.properties
```
jakarta-direct.properties（指定具体替换规则）
```properties
javax.faces.FACELETS_BUFFER_SIZE=jakarta.faces.FACELETS_BUFFER_SIZE
```
<a name="kfp8y"></a>
#### 指定文本更新
**作用域以及作用方式：** 理论上只要是UTF8编码的文件就可以，对指定条件的文件进行文本替换，将所有匹配到的文本替换成指定文本（比较傻瓜，主要用于其他规则没覆盖到的文件如xml等）。采用两层properties文件来配置，第一层指定对符合条件的文件使用哪个规则文件，第二层中指定具体的替换规则<br />**命令参数：**-tf,--text <配置文件路径> <br />**例子：**
```bash
-tf /xxx/xxx/jakarta-text-master.properties
```
jakarta-text-master.properties（指定要对哪类文件使用哪个规则文件）
```properties
*.tld=jakarta-direct.properties,jakarta-renames.properties
application.xml=jakarta-application-xml.properties,jakarta-direct.properties,jakarta-renames.properties
```
jakarta-web-xml.properties（指定具体替换规则）
```properties
version\='3.1'=version='5.0'
web-app_3_1.xsd=web-app_5_0.xsd
web-fragment_3_1.xsd=web-fragment_5_0.xsd
```
<a name="yXqZ6"></a>
#### 指定跳过规则和编码规则
**作用域以及作用方式：** 所有文件都可以，可以配置某几个或某几类文件的解码格式（比如有些文件可能包含其他语言），也可以配置某几个或某几类文件在转换过程中被跳过（不执行所有转换规则）<br />**命令参数：**-ts,--selection <配置文件路径>   <br />**例子：**
```bash
-ts /xxx/xxx/jakarta-selection.properties
```
jakarta-selection.properties（指定具体规则）
```properties
# 跳过所有pom.xml文件，!代表排除
*/pom.xml=!
# 跳过所有rose开头的文件
rose/pom.xml=!
# 所有foo开头的文件采用ISO-8859-1解码
foo/*=*=ISO-8859-1
```
<a name="X4mg1"></a>
#### pom.xml转换
**作用域以及作用方式：** 只对pom.xml文件生效，可以用来转换pom.xml的各个部分（目前根据当前的场景只支持了对module信息和dependency信息的更改，理论上可以支持pom.xml的任何部分）。还可以用来指定哪些module需要被转换（该作用当前只在Maven插件生效）<br />**命令参数：**-tm,--pom <配置文件路径>   <br />**例子：**
```bash
-tm /xxx/xxx/jakarta-pom.json
```
jakarta-pom.json（指定具体规则）
```json
{
  ### modules代表该项目中需要被转换的module，不在其中的会被跳过
  "modules": [
    {
      ### 该module的原始groupId（必填）
      "groupId": "com.aaa.bbb",
      ### 该module的原始artifactId（必填）
      "artifactId": "xxx-parent",
      ### 该module的目标groupId（选填）
      "targetGroupId": "com.aaa.bbb",
      ### 该module的目标artifactId（选填）
      "targetArtifactId": "xxx-parent-jakarta",
      ### 该module的目标version（选填）
      "targetVersion": "0.0.1-jakarta-SNAPSHOT"
    },
    {
      "groupId": "com.aaa.bbb",
      "artifactId": "xxx-service",
      "targetGroupId": "com.aaa.bbb",
      "targetArtifactId": "xxx-service-jakarta",
      "targetVersion": "0.0.1-jakarta-SNAPSHOT"
    },
    {
      "groupId": "com.aaa.bbb",
      "artifactId": "xxx-model",
      "targetGroupId": "com.aaa.bbb",
      "targetArtifactId": "xxx-model-jakarta",
      "targetVersion": "0.0.1-jakarta-SNAPSHOT"
    }
  ],
  ### dependencies代表该项目中需要被转换的依赖项（dependency），不在其中的会被跳过，会对所
      有选中的module生效
  "dependencies": [
    {
      ### 该module的原始groupId（必填）
      "groupId": "com.aaa.devapi",
      ### 该module的原始artifactId（必填）
      "artifactId": "client-sdk",
      ### 该module的目标groupId（选填）
      "targetGroupId": "com.aaa.devapi",
      ### 该module的目标artifactId（选填）
      "targetArtifactId": "client-sdk-jakarta",
      ### 该module的目标version（选填）
      "targetVersion": "1.0.0-jakarta"
    },
    {
      "groupId": "com.aaa.ccc",
      "artifactId": "artifactA",
      "targetGroupId": "com.aaa.ccc",
      "targetArtifactId": "artifactA",
      "targetVersion": "1.2.5-jakarta"
    }
  ]
}
```
注意：如果有选填的key-value项没有写出，比如一个module没有写targetGroupId，则直接会用其原始值
```json
### 比如
{
  "groupId": "com.aaa.xxx",
  "artifactId": "artifactA",
  "targetVersion": "1.2.5-jakarta"
}
### 就相当于
{
  "groupId": "com.aaa.xxx",
  "artifactId": "artifactA",
  "targetGroupId": "com.aaa.xxx",
  "targetArtifactId": "artifactA",
  "targetVersion": "1.2.5-jakarta"
}
```
<a name="mnQhO"></a>
#### 指定单条规则
**作用域以及作用方式：**作用域根据规则类型而定。该规则不是单独的一种规则，而是允许用户直接去指定单条前面所说的几种规则。前面几种转换都需要依赖于properties文件，如果在不想使用properties文件或者在properties文件外额外增加规则的话，就可以采用这种规则。<br />**命令参数：**-ti,--immediate ( tr | tv | td | tf ) key value<br />**例子：**命令行参数（一个-ti后只能跟随一条规则）
```bash
-ti tf pom.xml /xxx/xxx/xxx.properties -ti tr javax.servlet.jsp.el jakarta.servlet.jsp.el -ti ts foo/* ISO-8859-1 -ti ts */pom.xml ! -ti tv jakarta.activation [2.0,3)
```
注意：目前指定单条规则只支持tr，tv，td，tf四种操作
<a name="TgJ31"></a>
## 使用
<a name="do1zJ"></a>
### 我是用户，如何使用
使用方式<br />当前主要有Maven插件，API和命令行三种使用方式，无论是哪种使用方式所做的转换都是上述几种类型，本质上只是接入方式不同。
<a name="JNOLq"></a>
#### API

1. 传入自己要添加的自定义规则以及想要使用的场景来构造CustomRules（场景是指使用哪种默认规则配置，现在仅支持Jakarta规则以及无规则）
```java
List<String> renameList = new ArrayList<>(Arrays.asList("alipay-api/src/main/resources/jakarta-renames-test1.properties", "alipay-api/src/main/resources/jakarta-renames-test2.properties"));

List<String> textList = new ArrayList<>(Collections.singleton("org.eclipse.transformer.jakarta/src/main/resources/org/eclipse/transformer/jakarta/jakarta-text-master.properties"));

List<String> immediatesList = new ArrayList<>(Arrays.asList("tr", "javax.servlet", "jaaaaa.servlet", "tr", "javax.servlet.http", "jaaaaa.servlet.http"));

CustomRules customRules = new CustomRules.CustomRulesBuilder()
.setContainerType(ContainerType.Jakarta)
.setRenames(renameList)
.setTexts(textList)
.build();
```

2. 传入输入输出路径构造ApiTransformOptions
```java
ApiTransformOptions options = new ApiTransformOptions(customRules,
  "/Users/someone/Documents/jars/xxx.jar",
  "/Users/someone/Documents/xxx.jar");
```

3. 构建Transformer并开始转换
```java
Transformer transformer = new Transformer(options);
Transformer.ResultCode rc = transformer.run();

if (rc != Transformer.ResultCode.SUCCESS_RC) {
    logger.error("fail to transform");
}
else {
    logger.info("success to transform");
}
```
具体API使用方式可以参考源码中sofa-api/src/main/java/org/eclipse/transformer/example包下的示例代码
<a name="rUsHp"></a>
#### 命令行
<a name="a0Hn7"></a>
##### 参数解释
| 参数 | 解释 | 例子 |
| --- | --- | --- |
| -d,--dryrun | 看起来是空运行的意思，但是现在这个参数并没有被用到 |  |
| -h,--help     | 打印帮助信息 |  |
| -i,--invert | 反转规则，比如配置的是javax到jakarta的规则，加上这个参数后可以实现jakarta到javax |  |
| -ldt,--logShowDateTime | 日志打印时间和日期 |  |
| -lf,--logFile <arg> | 日志文件 |  |
| -ll,--logLevel <arg> | 日志级别 | -ll debug |
| -ln,--logName <arg>               | 日志名称 |  |
| -lp,--logProperty <arg>  | 日志属性 |  |
| -lpf,--logPropertyFile <arg> | 日志属性文件 |  |
| -o,--overwrite                    | 允许覆盖输出路径的文件 |  |
| -q,--quiet                        | 将日志级别设置为error |  |
| -t,--type <arg> | 输入文件类型 |  |
| -td,--direct <arg>  | 指定常量替换规则配置文件 | -tr /xxx/xxx/xxx.properties |
| -tf,--text <arg>  | 指定文件到使用的配置文件的映射 | -tr /xxx/xxx/xxx.properties |
| -tr,--renames <arg>  | 指定Package重命名规则配置文件 | -tr /xxx/xxx/xxx.properties |
| -ts,--selection <arg>             | 指定文件编码以及文件跳过规则配置文件 | -ts /xxx/xxx/xxx.properties |
| -tv,--versions <arg>             | 指定Package版本范围规则配置文件 | -tv /xxx/xxx/xxx.properties |
| -tm,--pom <arg> | 指定 | -tm /xxx/xxx/xxx.json |
| -ti,--immediate ( tr &#124; tv &#124; td &#124; tf ) key value | 指定追加的单条配置（这里面tr &#124; tv &#124; tb &#124; td &#124; tf 这些参数后面放的不再是规则的配置文件而是单条的规则） | -ti tf pom.xml /xxx/xxx/xxx.properties -ti tr javax.servlet.jsp.el jakarta.servlet.jsp.el -ti ts foo/* ISO-8859-1 -ti ts */pom.xml ! -ti tv jakarta.activation [2.0,3)<br />对pom.xml文件使用jakarta-pom-xml.properties中配置的规则，一个-ti只包括一个规则，多个规则要多个-ti |
| -u,--usage                    | 展示用法（然而就一行没什么用） |  |
| -v,--verbose                      | 将日志级别设置为debug |  |
| -x,--trace                       | 将日志级别设置为trace |  |
| -w,--widen                        | 允许archive nesting，即允许Jar中包含Jar和Zip，以及Rar，War，Ear，Zip中包含Zip |  |

<a name="Lym2C"></a>
##### 一个完整的例子
```bash
/aaa/bbb/ccc-sdk-1.0.0.20230608.jar /aaa/bbb/ccc-sdk-1.0.0.20230608-jakarta.jar -ti tf pom.xml jakarta-pom-xml.properties -tr jakarta-renames-test.properties -ti tr javax.servlet.jsp.el=jakarta.servlet.jsp.el -tf /xxx/xxx/jakarta-text-master.properties -ts /xxx/xxxjakarta-selection.properties -td /xxx/xxx/jakarta-direct.properties -tv /xxx/jakarta-versions.properties
```

注意：

- 各个命令（规则）有优先级，简单来说-ti>-tr, -tb, -td, -tf, -tr, -ts, -tm>默认规则。-tr, -tb, -td, -tf, -tr, -ts, -tm这几个指定规则配置文件的参数会覆盖掉选择的默认规则（比如jakarta规则），所以如果想保留选择的默认规则需要使用-ti来逐条追加规则（后续可以改造一下ti命令），而-ti指令所指定的规则会覆盖掉tr，tv，td，tf等指令所指定的配置文件中键值一样的规则
```bash
# 假设参数如下，其中jakarta-renames-test.properties中有javax.servlet.jsp.el=xxx.servlet.jsp.el，这种情况下最后规则为c
-tr jakarta-renames-test.properties -ti tr javax.servlet.jsp.el=jakarta.servlet.jsp.el
```

- -tr, -tb, -td, -tf, -tr, -ts这些指令指定的规则配置文件支持绝对路径
- -tr, -tb, -td, -tf, -tr, -ts这些指令都支持通过多次使用来指定多个规则配置文件，同一类规则下的多个配置文件中的规则会被合并，如果多个配置文件中出现键值相同的规则项则最后的配置文件会将前面的覆盖掉
```bash
# 通过添加以下两个指令能将这两个
-tr jakarta-renames-test1.properties -tr jakarta-renames-test2.properties

# 假设jakarta-renames-test1.properties如下
javax.activation=jakarta.activation
javax.annotation.security=jakarta.annotation.security
javax.batch.api.chunk=xxxxx.batch.xxx.chunk

# 假设jakarta-renames-test2.properties如下
javax.batch.api.chunk=jakarta.batch.api.chunk
javax.batch.api.listener=jakarta.batch.api.listener

# 则最后的规则会变为：
javax.activation=jakarta.activation
javax.annotation.security=jakarta.annotation.security
javax.batch.api.chunk=jakarta.batch.api.chunk
javax.batch.api.listener=jakarta.batch.api.listener
```

- 单类规则支持多个配置文件
- 如果要对xml，properties这两类文件追加自定义的替换规则，需要加在tr（rename）指令对应的规则配置文件中，不要加在tf指令对应的规则配置文件中，否则会导致tr（rename）指令配置的规则不会在添加规则的xml，properties这两类文件中执行
```bash
# 假设想对pom.xml文件添加如下规则：
<artifactId>alipay-xxx</artifactId>=<artifactId>alipay-xxx-jakarta</artifactId>

# 而tr指令的规则配置文件中有：
javax.servlet=jakarta.servlet
# pom.xml文件中有：
<groupId>javax.servlet</groupId>

# 正常执行的时候<groupId>javax.servlet</groupId>会被替换为：
<groupId>jakarta.servlet</groupId>

# 如果现在将想要添加的规则放在一个新配置文件jakarta-pom-xml.properties中，然后使用-ti tf追加pom.xml与该配置文件的链接
pom.xml=jakarta-pom-xml.properties

# 这种情况下<artifactId>alipay-xxx</artifactId>=<artifactId>alipay-xxx-jakarta</artifactId>这条规则会执行，而javax.servlet=jakarta.servlet这条规则不会被执行

# 所以应该将追加的规则放在tr指令的规则配置文件中：
<artifactId>alipay-xxx</artifactId>=<artifactId>alipay-xxx-jakarta</artifactId>
javax.servlet=jakarta.servlet
```
<a name="hPpab"></a>
#### 使用建议
Q：我只想做对我的Jar最简单的javax到jakarta的替换，没有要自定义的规则<br />A：直接指定好目标文件和输出文件的路径就ok了，不需要加任何参数
```json
/aaa/bbb/ccc-sdk-1.0.0.jar /aaa/bbb/ccc-sdk-1.0.0-jakarta.jar
```
```xml
<plugin>
    <groupId>org.eclipse.transformer</groupId>
    <artifactId>jakarta-transformer-maven-plugin</artifactId>
    <version>0.6.0-sofa-SNAPSHOT</version>
    <configuration>
      <rules>
          <pom>conf/test.json</pom>
      </rules>
  </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>jakarta</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
```java
CustomRules customRules = new CustomRules.CustomRulesBuilder()
.setContainerType(ContainerType.Jakarta)
.build();

ApiTransformOptions options = new ApiTransformOptions(customRules,
  "/Users/someone/Documents/jars/xxx.jar",
  "/Users/someone/Documents/xxx.jar");

Transformer transformer = new Transformer(options);
Transformer.ResultCode rc = transformer.run();
```

<a name="EoUMw"></a>
### 我是开发者，如何扩展
目前可扩展的主要有两个方向：

- 扩展到新场景，当前仅配置了Java EE转换到Jakarta EE的场景，如果有其他需要对Java构建产物进行转换的场景也可以通过配置规则来使用该工具进行转换。
- 扩展支持的文件类型以及修改操作。

Transformer有别于其他类似工具最大的特点在于能够对构建产物如JAR包，Class文件进行文本替换，如果有其他场景需要这种能力则可以考虑Transformer。

