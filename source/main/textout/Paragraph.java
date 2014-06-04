package textout;

public class Paragraph extends Document {
    private boolean separate = true;

    public Paragraph(Style parent) {
        super(parent);
    }

    @Override
    public boolean processAttribute(String name, String value) {
        if (name.equals("separate")) {
            if (value.equals("true")) separate = true;
            else if (value.equals("false")) separate = false;
            else throw new IllegalArgumentException ("Invalid value \"" +
                value + "\" for \"separate\" attribute");
        } else if (name.equals("bullet")) setBullet(value);
        else if (name.equals("leftQuote")) setLeftQuote(value);
        else if (name.equals("rightQuote")) setRightQuote(value);
        else if (name.equals("innerQuote")) setInnerQuote(value);
        else return (super.processAttribute(name, value));
        return(true);
    }

    public boolean isSeparated() {
        return(separate);
    }
}
