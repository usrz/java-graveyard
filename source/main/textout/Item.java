package textout;

public class Item extends Style {

    public Item(Style parent) {
        super(parent);
        if (! (parent instanceof List)) {
            throw new IllegalArgumentException("The <item> tag can only be " +
                "a child of a <list> tag");
        }
    }

    public boolean isSeparated() {
        return (((List)getParent()).isSeparated());
    }

    @Override
    public void apply(TextWriter out) {
        List list = (List)getParent();
        setBullet(list.next() + list.getBullet());
        super.apply(out);
    }
}
