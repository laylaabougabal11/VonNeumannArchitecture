package components;

public class Register {
    private String name;
    private int data;

    public Register(String name) {
        this.name = name;
        data = 0;
    }

    public int read() {
        return data;
    }

    public void write(int data) {
        if (name.equals("R0"))
            this.data = 0;
        else {
            if (data < -2147483647 || data > 2147483647)
                System.out.println("Data out of bounds");
            this.data = data;
        }
    }

    public String getName() {
        return name;
    }

    public int getData() {
        return data;
    }

    public void dump() {
        System.out.println(data);
    }

    public String dumpString() {
        return "Name " + name + " Data " + Integer.toString(data);
    }
}
