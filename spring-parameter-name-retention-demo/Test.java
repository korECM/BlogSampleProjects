public class Test {
    private String myStr;

    public Test (String s) {
     this.myStr = s;
    }

    public void print () {
     System.out.println(myStr);
    }

    public static void main (String[] args) {
     Test test = new Test("test");
     test.print();
    }
 }