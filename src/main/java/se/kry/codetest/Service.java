package se.kry.codetest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Service implements Serializable {

    private String Url;
    private String Name;
    private String Response;
    private LocalDateTime DateTime;
    private String AddedByUser;

    public Service(String name, String url, String addedByUser) {
        Url = url;
        Name = name;
        AddedByUser = addedByUser;
        DateTime = LocalDateTime.now();
        Response = "Unknown";
    }

    public Service(String url, String name, String response, LocalDateTime dateTime, String addedByUser) {
        Url = url;
        Name = name;
        Response = response;
        DateTime = dateTime;
        AddedByUser = addedByUser;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getResponse() {
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }

    public LocalDateTime getDateTime() {
        return DateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        DateTime = dateTime;
    }

    public String getAddedByUser() {
        return AddedByUser;
    }

    public void setAddedByUser(String addedByUser) {
        AddedByUser = addedByUser;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public String toString() {
        return "Service{" +
                "Url='" + Url + '\'' +
                ", Name='" + Name + '\'' +
                ", Response='" + Response + '\'' +
                ", DateTime=" + DateTime +
                ", AddedByUser='" + AddedByUser + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(Url, service.Url) &&
                Objects.equals(Name, service.Name) &&
                Objects.equals(Response, service.Response) &&
                Objects.equals(DateTime, service.DateTime) &&
                Objects.equals(AddedByUser, service.AddedByUser);
    }

}
