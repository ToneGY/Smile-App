# Smile APP实验报告

> 王耕宇 17307110209

[toc]

## 一、UI风格

该APP着重注意了UI风格和交互效果，对于UI界面，在字体、颜色等方面进行了精心的选取，尤其是颜色多在[中国传统色](http://zhongguose.com/)和[莫兰迪经典色](https://watermoon333.pixnet.net/blog/post/51352420-230%E6%AC%BE%E8%8E%AB%E8%98%AD%E8%BF%AA%E8%89%B2%E7%A2%BC%EF%BD%9C%E5%84%AA%E9%9B%85%E9%AB%98%E7%B4%9A%E7%81%B0%EF%BD%9C%E8%8E%AB%E8%98%AD%E8%BF%AA%E8%89%B2%E5%8D%A1%E4%B8%80)中选取，使得整个页面协调美观。

对于用户交互的动画效果，APP在很多地方进行了精心设计。点击界面下方的导航栏时，会自动由亮色切换成暗色；在小组事项的成员展示中、APP使用了带箭头转动效果的下拉列表展示成员信息；在作业列表的更新中，我们也使用了下拉更新的动态效果，同时实现了直接点击进行刷新的功能；在待办事项长按删除时，我们使底部导航栏自动隐藏等等。

## 二、技术实现

### 1. 基础组件的使用

APP使用了`PopupWindow`、`Handler`、`Fragment`、`Service`、` BroadcastReceiver`、`Thread`、`AlertDialog`、`FileProvider`、`WindowManager`、`Menu`、`Notification`、`AlarmManager`等多个基本组件。

在内容存储方面，APP使用了`SharedPreferences`、`SQLite`、内部存储、外部存储、远程`Mysql`存储等多种存储方式。

在UI显示方面，APP使用了`RecyclerView`、`CardView`、`GridView`、`AbsListView`、`AnimatedExpandableListView`、`FrameLayout`、`RoundedImageView`、`BubbleChart`、`ExpandableLayout`、`RadioGroup`、`LineChart`、`SpinKitView`、`SrollView`、`ProgressBar`、`TabLayout`、`ViewPager2`，同时，我们自主实现了`RippleView`组件。

在动画设计方面，APP实现了引入、退出、旋转、抖动等多种动画形式。

在显示效果方面，APP对轮廓效果、阴影效果、点击效果等多个方面进行了单独的实现，具体实现了52个`xml`文件用于显示效果。

### 2. Cookie持久化与预加载

由于对网页的爬取对很慢，因此我们的很多努力都用在了降低`Elearning`操作时延上面。

在对`Elearning`功能进行实现的过程中，我们发现如果每次操作都执行重新登录操作，则注定会减慢界面的加载速度，降低用户的使用体验，因此，我们实现了`Cookie`的持久化，将首次登录后的`Cookie`通过`SharedPreferences`进行保存，降低了加载时间。

对于登录后课程、文件、作业信息的直接获取同样较为缓慢，因此我们选择将一些不会变动的信息优先存储在本地，打开时从本地读取。同时运行一个`Service`，在后台对数据进行重新获取与更新。对于一些挤占网络带宽的加载，我们决定将APP决定权交给用户。通过在作业界面设置`header`来显示上次更新时间，使用户下拉或者点击更新进行重新加载。

### 3. 三级列表的使用

为了能够简洁、完整地向用户展示作业信息，我们实现了三级列表的技术，对每一级列表，我们都确保了其具有被点击的功能和展开效果，虽然原理上并不难，但是实现上较为复杂，耗费了我们大量的时间。

### 4. 数据可视化

我们借助了`mikephil`插件，实现了使用折线图对代码数据的统计可视化，该折线图可进行滑动查看。同时，我们使用Hash表将横坐标显示为日期，同时实现了不同折线的不同阴影效果。我们在该视图下添加了`RecyclerView`组件，在点击图表的同时进行响应，显示相应日期内的待办事项。

### 5. 数据同步

对于小组数据的同步，我们选择在远程终端中使用`SpringBoot`运行后端，实现小组间数据的同步。

对于不同`Fragment`之间的数据同步，例如在`Elearning`页面我们选择使用三个`Fragment`进行展示，但是这三个`Fragment`中有些数据例如课程信息是共享的，为了实现三个`Fragment`数据一次获取，我们大量使用了`BroadcastReceiver`来进行`Fragment`或者`Activity`之间的数据通信。

对于使用`okhttp`的数据获取或者对本地文件的获取，由于新版`Android`不允许该操作在主线程中使用，因此我们为这些操作创建了子线程，并大量使用`Handler`和`Message`组件实现主从线程间的数据通信。

### 6. 待办提醒

我们没有做到真正意义上的程序保活，但是我们通过使用前台进程，确保了用户不主动杀死APP的情况下，所有待办事项会在截止时间的前30分钟进行事项提醒。具体执行方法是从所有待办中获取最早需要提醒的时间，然后使用`AlarmManager`设置具体的提醒时间，再使用`BroadcastReceiver`进行接收，这一方法经测试无法在程序被杀死后重新唤醒程序。

## 三、功能介绍

### 1. 个人待办

#### 主界面

主界面主要借助`RecyclerView`实现列表的展示，同时我们使用`clock`组件在左上角展示了当前时间。每个列表项实现了点击和长按两种操作，点击进入事项详情，长按进行多选删除。如果点击右上角的加号，则进入添加事项界面。点击另一个按钮，进入数据可视化界面。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\f06a76fe3c5f69de55cf453f3985143.jpg" alt="f06a76fe3c5f69de55cf453f3985143" style="zoom: 16%;" />

#### 增删改查

在个人待办方面，这款APP实现了基本的增删改查功能。在待办内容上，APP实现了时间的滑动列表选取功能；同时，APP设置了四个事项重要等级，每个等级对应了不同的颜色，选取相应的颜色会直接映射到标题上。删除操作采用长按多项选择的方法。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\efa7e4a6afe6424a0aa41855a9d512a.jpg" alt="efa7e4a6afe6424a0aa41855a9d512a" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\32d2a893de7dd97e9b241ebb0d93ca0.jpg" alt="32d2a893de7dd97e9b241ebb0d93ca0" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\a1dab8f8d5a2a028224ecd36906c1bc.jpg" alt="a1dab8f8d5a2a028224ecd36906c1bc" style="zoom:16%;" />

#### 到期提醒

我们通过使用前台进程，较大限度地保证APP不被杀死。在这个基础上，我们规定截止日期之前30分钟进行事项提醒。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\19cf7b363adcb7c841e8aae8c5f3765.jpg" alt="19cf7b363adcb7c841e8aae8c5f3765" style="zoom:16%;" />

#### 统计可视化

在该基础之上，APP实现了对待办数据的可视化统计。统计采用图标的形式进行展示，纵坐标对应到具体某天，其中当天显示为“今天”。图中显示四条折线，反映出每个重要等级的事项数量及其变化趋势。点击某一天的对应区域，会显示当天存在的具体待办事项，可通过点击此事项进行查看和编辑。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\12f820db98429b52f44b4609a5ab6cc.jpg" alt="12f820db98429b52f44b4609a5ab6cc" style="zoom:16%;" />

### 2、小组事项

#### 远程登录

我们通过与远端的服务器连接，实现小组合作的待办形式。该情况需要先通过第四级页面进行注册后登录使用。若未登录则提示登录。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\f6d758714949357e82d2f25162439e9.jpg" alt="f6d758714949357e82d2f25162439e9" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\0d6664477e8ace63f462a1795c2e235.jpg" alt="0d6664477e8ace63f462a1795c2e235" style="zoom:16%;" />

#### 小组协作

该主界面主要包括了小组名称、组员信息以及具体事项。

其中组员信息采用下拉列表的形式实现，右上角箭头具有旋转动画效果。在具体事项方面，显示效果与个人事项相同。小组切换通过点击右上角按钮弹出选择框后进行操作。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\9f7d7c563299dda2ee60dca90ad90eb.jpg" alt="9f7d7c563299dda2ee60dca90ad90eb" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\82082869ddd40f709bd5596ac963e31.jpg" alt="82082869ddd40f709bd5596ac963e31" style="zoom:16%;" />

### 3. Elearning

APP实现了对`elearning`文件及作业信息的获取，该页面采用`TabLayout`和`ViewPaper`组件实现页面的滑动切换。该部分主要包括课程、作业和文件三块内容。

#### 登录

考虑额elarning账号的安全性，我们选择对其账号进行本地存储，点击右上角头像，会弹出子工具栏，其支持重新登录、退出登录及切换头像的功能。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\e4b198078a40211a0bc34b7fcd40fd9.jpg" alt="e4b198078a40211a0bc34b7fcd40fd9" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\87de15bf72fafe6c148609ad94419fd.jpg" alt="87de15bf72fafe6c148609ad94419fd" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\52df761c5423429d972fb048dd71ff5.jpg" alt="52df761c5423429d972fb048dd71ff5" style="zoom:16%;" />

#### 课程

在课程层面，我们使用`GridView`对课程所有课程进行展示。点击进入课程后可查看该课程所有文件信息，包括文本文件及目录文件。通过点击具体文件，可对其进行下载，下载过程中会展示下载进度。APP对常用的文件后缀进行了图标映射，确保了文件类型清晰可见。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\498c334adfa002e46ee7fc128b082eb.jpg" alt="498c334adfa002e46ee7fc128b082eb" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\beb9cda2e6b6135b86b80db87b99e68.jpg" alt="beb9cda2e6b6135b86b80db87b99e68" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\13c1f0966e84e4440ea1197cab34256.jpg" alt="13c1f0966e84e4440ea1197cab34256" style="zoom:16%;" />

#### 作业

作业部分实现了三级列表，第一级展示课程，第二级展示该课程下的作业项，第三级展示该作业的部分信息，在第三集列表中点击“查看详情”按钮，可以直接进入到作业详情信息。该信息中包含附带文件以及提交文件，点击文件可以进行下载。同时，该页面还可以展示作业给分情况以及评论。

另外一个比较主要的功能是实现了手动的刷新，点击最近更新时间或者下拉列表会进行文件信息的重新获取。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\ac1070295f64437ad3c77ce32e41fbd.jpg" alt="ac1070295f64437ad3c77ce32e41fbd" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\526c385048db0d5b252835e08b4d649.jpg" alt="526c385048db0d5b252835e08b4d649" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Roaming\Typora\typora-user-images\image-20230105195457827.png" alt="image-20230105195457827" style="zoom:16%;" />

#### 文件

该部分主要存储并展示下载后的文件，长按文件项会弹出菜单栏，可执行删除、多选、转发、移动、重命名等功能。同时，我们内置了多个插件，支持对pdf、markdown等多种格式文件的直接查看，对于不具备APP内部查看的文件类型，使用`FileProvider`选择其他APP进行打开。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\a00c19635ddc4f7f84d768343a20fbe.jpg" alt="a00c19635ddc4f7f84d768343a20fbe" style="zoom:16%;" />



<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\d9c03bea4d5db14e4e8a46e50e47080.jpg" alt="d9c03bea4d5db14e4e8a46e50e47080" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\6dfd21a80a7a66d0c338aed7060e1d7.jpg" alt="6dfd21a80a7a66d0c338aed7060e1d7" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\319c48abbc5512f653b700ffee2de77.jpg" alt="319c48abbc5512f653b700ffee2de77" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\2a9d493db030db310d9a7d1155ab787.jpg" alt="2a9d493db030db310d9a7d1155ab787" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\105fed46b0b7ee74efdbcea11040438.jpg" alt="105fed46b0b7ee74efdbcea11040438" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\2b5b05d25cec56749d3091e6664b9d1.jpg" alt="2b5b05d25cec56749d3091e6664b9d1" style="zoom:16%;" />

### 4. 个人信息

对于个人信息页面的设计比较简单，主要包括一些功能设置和软件更新，其中部分功能尚未实现。

若用户未登录，则显示登录页面。登录后会获取远程用户信息、小组信息、待办信息等。

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\8f161272887108f50afd35ddac12e2f.jpg" alt="8f161272887108f50afd35ddac12e2f" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\0d6664477e8ace63f462a1795c2e235.jpg" alt="0d6664477e8ace63f462a1795c2e235" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\f78ce919b2371181eccb90874c29466.jpg" alt="f78ce919b2371181eccb90874c29466" style="zoom:16%;" />

<img src="C:\Users\98157\AppData\Local\Temp\WeChat Files\a4cd90095780d6b3932195e4f830a2c.jpg" alt="a4cd90095780d6b3932195e4f830a2c" style="zoom:16%;" />



## 四、代码结构

![image-20230105214053939](C:\Users\98157\AppData\Roaming\Typora\typora-user-images\image-20230105214053939.png)

<img src="C:\Users\98157\AppData\Roaming\Typora\typora-user-images\image-20230105214150289.png" alt="image-20230105214150289" style="zoom:80%;" />

<img src="C:\Users\98157\AppData\Roaming\Typora\typora-user-images\image-20230105214206586.png" alt="image-20230105214206586" style="zoom:80%;" />

<img src="C:\Users\98157\AppData\Roaming\Typora\typora-user-images\image-20230105214220137.png" alt="image-20230105214220137" style="zoom:80%;" />

<img src="C:\Users\98157\AppData\Roaming\Typora\typora-user-images\image-20230105214233235.png" alt="image-20230105214233235" style="zoom:80%;" />

<img src="C:\Users\98157\AppData\Roaming\Typora\typora-user-images\image-20230105214247574.png" alt="image-20230105214247574" style="zoom: 80%;" />

## 五、总结与不足

### 1. 总结

该APP实现了待办列表的创建、到期提醒、小组远程共享以及数据的可视化。同时还实现了Elearning课程的文件、作业信息的查看与获取，以及相应的文件管理技术。在这一过程中，APP使用了很多的技术。同时，该APP注重UI的展示与设计，包含了8个静态动画设计以及多个动态设计，使用了共8中字体进行展示，使用了50多个文件对展示的边框、阴影、点击效果等进行设计。`Elearning`功能虽然采用了爬虫技术，但是使用多种方法降低了程序的操作时延，保证了程序的流畅性。

### 2. 不足

在设计该APP的过程中，我对很多技术进行了研究，其中最耗时的就是保活技术，但是最终没有完美实现。虽然本学期有三分之一的时间都用到了该APP的代码上，但是仍有很多功能没有实现，比如个人待办和小组待办之间的相互转换、待办的富文本编辑、小组成员之间的聊天室等等。由于时间的原因，这些功能只能在课程结束后继续将其补齐，将这款APP真正做好。
