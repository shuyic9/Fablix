public class Star {

    private String id;

    private String name;

    private int birthYear;

    public Star (){
        name = "";
        birthYear = 0;
    }

    public Star (String id, String name, int birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("Id:" + getId());
        sb.append(", ");
        sb.append("Name:" + getName());
        sb.append(", ");
        sb.append("BirthYear:" + getBirthYear());

        return sb.toString();
    }


    public String toCSVFormat() {
        return String.format("%s,%s,%s", getId(), getName(), getBirthYear());
    }
}
