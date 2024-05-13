import java.util.HashSet;
import java.util.Set;

public class Movie {

    private String id;

    private String title;

    private int year;

    private String director;

    private Set<String> genres;

    public Movie (){
        id = "";
        genres = new HashSet<>();
    }

    public Movie (String id, String title, Integer year, String director) {
        this.setId(id);
        this.setTitle(title);
        this.setYear(year);
        this.setDirector(director);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDirector() { return director; }

    public void setDirector(String director) { this.director = director; }

    public Set<String> getGenres() { return genres; }

    public void setGenres(Set<String> genres) { this.genres = genres; }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("Id:" + getId());
        sb.append(", ");
        sb.append("Title:" + getTitle());
        sb.append(", ");
        sb.append("Year:" + getYear());
        sb.append(", ");
        sb.append("Director:" + getDirector());
        sb.append(", ");
        sb.append("Genres:" + getGenres());

        return sb.toString();
    }


    public String toCSVFormat() {
        return String.format("%s,%s,%d,%s", id, title, year, director);
    }
}
