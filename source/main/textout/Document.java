package textout;

public class Document extends Style {

    public Document(Style parent) {
        super(parent);
    }

    @Override
    public boolean processAttribute(String name, String value) {
        if (name.equals("indent")) setIndent(value);
        else if (name.equals("margin")) setMargin(value);
        else if (name.equals("align")) setAlignment(value);
        else return(super.processAttribute(name, value));
        return(true);
    }
}
