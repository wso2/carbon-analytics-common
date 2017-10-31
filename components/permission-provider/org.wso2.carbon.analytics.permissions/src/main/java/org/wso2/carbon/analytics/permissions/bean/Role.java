package org.wso2.carbon.analytics.permissions.bean;

/**
 * Role bean class.
 */
public class Role {
    private String id;
    private String name;

    /**
     * Default constructor.
     */
    public Role() {
    }

    /**
     * Constructor with role Id, name parameters.
     *
     * @param id
     * @param name
     */
    public Role(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get role Id.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Set role Id.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get role name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set role name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Overrides to string.
     *
     * @return
     */
    @Override
    public String toString() {
        return "Role[id=" + id + ", name=" + name + "]";
    }
}
