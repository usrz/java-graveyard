package textout;

public class Rule extends Break {
    private char style = '-';

    public Rule(Style parent) {
        super(parent);
    }

    @Override
    public boolean processAttribute(String name, String value) {
        if (name.equals("style")) {
            if (value.length() == 1) style = value.charAt(0);
            else throw new IllegalArgumentException ("Invalid value \"" +
                value + "\" for \"style\" attribute");
        } else {
            return (super.processAttribute(name, value));
        }
        return(true);
    }

    public char getStyle() {
        return(style);
    }
}
