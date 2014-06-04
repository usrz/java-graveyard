package textout;

public class List extends Paragraph {

    private boolean separate = false;

    private int index = 1;

    public List(Style parent) {
        super(parent);
        setBullet(") ");
    }

    @Override
    public boolean processAttribute(String name, String value) {
        if (name.equals("start")) {
            index = Integer.parseInt(value);
        } else if (name.equals("separate")) {
            if (value.equals("true")) separate = true;
            else if (value.equals("false")) separate = false;
            else throw new IllegalArgumentException ("Invalid value \"" +
                value + "\" for \"separate\" attribute");
        } else {
            return (super.processAttribute(name, value));
        }
        return(true);
    }

    @Override
    public boolean isSeparated() {
        return(separate);
    }

    public int next() {
        return (index++);
    }
}
