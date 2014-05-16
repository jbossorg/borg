package org.jboss.planet.model;

/**
 * Tags Group entity. Group of blog posts based on defined tags
 *
 * @author Libor Krzyzanek
 */
public class TagsGroup {

	private Integer id;

	protected String name;

	protected String title;

	protected boolean showInMenu = false;

	protected Long menuOrder;

	protected String tags;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isShowInMenu() {
		return showInMenu;
	}

	public void setShowInMenu(boolean showInMenu) {
		this.showInMenu = showInMenu;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}


	public Long getMenuOrder() {
		return menuOrder;
	}

	public void setMenuOrder(Long menuOrder) {
		this.menuOrder = menuOrder;
	}

	@Override
	public String toString() {
		return "TagsGroup{" +
				"id=" + id +
				", name='" + name + '\'' +
				", title='" + title + '\'' +
				", showInMenu=" + showInMenu +
				", menuOrder=" + menuOrder +
				", tags='" + tags + '\'' +
				'}';
	}
}
