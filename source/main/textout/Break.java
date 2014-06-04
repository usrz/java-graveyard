package textout;

public class Break extends Document {
    private boolean separate = false;

    public Break(Style parent) {
        super(parent);
    }

    @Override
    public boolean processAttribute(String name, String value) {
        if (name.equals("separate")) {
            if (value.equals("true")) separate = true;
            else if (value.equals("false")) separate = false;
            else throw new IllegalArgumentException ("Invalid value \"" +
                value + "\" for \"separate\" attribute");
        } else {
            return (super.processAttribute(name, value));
        }
        return(true);
    }

    public boolean isSeparated() {
        return(separate);
    }
}
