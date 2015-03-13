

&lt;project name="codecode" default="dist" basedir="."&gt;


> 

&lt;description&gt;


> > Chép mã nguồn vào thư mục đích để chương trình đọc được.

> 

&lt;/description&gt;


> 

&lt;target name="dist"&gt;


> > 

&lt;mkdir dir="bin/src" /&gt;


> > 

&lt;sync todir="bin/src"&gt;


> > > 

&lt;fileset dir="src/path/to/package" /&gt;



> > 

&lt;/sync&gt;



> 

&lt;/target&gt;




&lt;/project&gt;


public class Main {

> public static void main(String[.md](.md) args) {
> > CodeBrowser.show("src", new Object[.md](.md)[.md](.md) {
> > > new Object[.md](.md) { "Hello world!", HelloWorld.class }
> > > new Object[.md](.md) { "BKPaint", BKPaint.class,
> > > > "RectangleTool.java", "CircleTool.java",  }

> > });

> }

}  }}} 
  Thay "path/to/package" bằng đường dẫn đến gói chứa mã nguồn chương trình. Nếu bạn chứa chương trình trong nhiều gói, có thể thêm các thẻ fileset khác miễn là không có tệp nào trùng tên. Khi chạy đoạn mã này (phải chuột > Run as > Ant build), mã nguồn sẽ được chép vào thư mục bin/src để chương trình có thể đọc được. Nếu không tạo tệp này, mỗi khi chạy chương trình bạn phải sao chép mã nguồn một cách thủ công.
  * Tự động cập nhật mã nguồn (tùy chọn): Phải chuột vào dự án, chọn Property. Trong hộp thoại chọn Builders, thêm Ant builder mới, chọn buildfile là tệp vừa tạo. Trong tab Targets, mục Auto build, đặt thành default target. Sau khi thiết lập xong chương trình khi thực hiện sẽ luôn đọc được mã nguồn mới nhất (nếu không phải chạy tệp copycode.xml bằng tay).
  * Viết các chương trình nhỏ extends từ lớp Runner và đặt mã thực thi vào hàm void run() thay vì hàm main() .
  Tạo lớp khởi động có dạng như sau: 
  {{{
  }}}
  Tham số "src" là thư mục chứa mã nguồn chương trình (sau khi dịch, bên trong thư mục bin). Tham số này cần phải khớp với thuộc tính "todir" trong tệp copycode.xml. Tham số thứ 2 là mảng các chương trình con. Mỗi mảng Object tương ứng với một chương trình tạo thành bởi 1 hoặc nhiều tệp .java. Phần tử đầu tiên là tên để hiển thị trong danh sách, phần tử thứ 2 là lớp chính, tiếp sau là các file hỗ trợ (nếu có). Có thể thêm vào đầu tiên một tham số chứa thông tin mô tả gói chương trình dưới dạng String hoặc InputStream.
  * Dịch và chạy chương trình. Cũng có thể xuất ra dạng .jar, chương trình vẫn hoạt động bình thường.```