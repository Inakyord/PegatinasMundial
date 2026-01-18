package es.upm.etsiinf.proyectofinalpegatinas.data.local;

public class Team {
    private String name;
    private String code;
    private String group;
    private String isoCode;

    public Team(String name, String code, String group) {
        this(name, code, group, null);
    }

    public Team(String name, String code, String group, String isoCode) {
        this.name = name;
        this.code = code;
        this.group = group;
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getGroup() {
        return group;
    }

    public String getIsoCode() {
        return isoCode;
    }
}
