package org.jboss.planet.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Tags Group entity. Group of blog posts based on defined tags
 *
 * @author Libor Krzyzanek
 */
@Entity
@Cacheable
public class TagsGroup implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Integer id;

	@NotNull
	@Column(unique = true)
	@Size(max = 32)
	@Pattern(regexp = "^[a-z0-9_-]*$", message = "Invalid name for tags group. Can contains a-z, 0-9, _ and -.")
	protected String name;

	@NotNull
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
