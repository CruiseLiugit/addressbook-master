package com.vaadin.tutorial.addressbook;

import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/* 
 * UI 类是程序的起始点，你可以把它 部署到 VaadinServlet 或者 VaadinPortlet  类中，使用 UI 参数名来配置
 * 当你的浏览器访问程序的时候，会自动生成一个 UI 页面
 * 或者你可以选择嵌入自己的 web 页面
 * 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("Addressbook")
public class AddressbookUI extends UI {

	/* User interface components are stored in session. */
	/* 用户界面组件，都保存在 session 中，session 失效，组件就销毁 */
	private Table contactList = new Table();
	private TextField searchField = new TextField();
	private Button addNewContactButton = new Button("新建");
	private Button removeContactButton = new Button("删除内容");
	private FormLayout editorLayout = new FormLayout();    //表单布局
	private FieldGroup editorFields = new FieldGroup();    //字段组

	private static final String FNAME = "姓氏";
	private static final String LNAME = "名称";
	private static final String COMPANY = "公司";
	private static final String[] fieldNames = new String[] { FNAME, LNAME,
			COMPANY, "Mobile Phone", "Work Phone", "Home Phone", "Work Email",
			"Home Email", "Street", "City", "Zip", "State", "Country" };

	/*
	 * 任何组件都可以绑定外部数据 源，这个例子仅仅使用了一个 在内存中虚拟的表。可以有更多的实践
	 * 
	 * Any component can be bound to an external data source. This example uses
	 * just a dummy in-memory list, but there are many more practical
	 * implementations.
	 */
	IndexedContainer contactContainer = createDummyDatasource();

	/*
	 * 所有的 UI 类创建，都必须执行 init() 
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	protected void init(VaadinRequest request) {
		initLayout();       //创建布局 
		initContactList();  //创建内容表格
		initEditor();       //创建编辑器
		initSearch();       //创建搜索栏
		initAddRemoveButtons(); //创建删除按钮
	}

	/*
	 * In this example layouts are programmed in Java. You may choose use a
	 * visual editor, CSS or HTML templates for layout instead.
	 */
	private void initLayout() {

		/* Root of the user interface component tree is set
		 * 所有组件的 根 在这里设置，水平分割布局的面板
		 *  */
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setContent(splitPanel);

		/* Build the component tree */
		//垂直布局
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);   //水平分割，左侧
		splitPanel.addComponent(editorLayout); //水平分割，右侧
		
		
		
		//水平布局，底部搜索框、增加内容按钮
		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.addComponent(searchField);
		bottomLeftLayout.addComponent(addNewContactButton);
		
		//把内容添加到布局中
		leftLayout.addComponent(contactList);  //表格
		leftLayout.addComponent(bottomLeftLayout);

		/* Set the contents in the left of the split panel to use all the space */
		leftLayout.setSizeFull();

		/*
		 * 设置膨胀比例，让两个组件所占 空间比例不一样。表格大、底部搜索框小
		 * 让表格占满右侧 除了 底部以外的空间
		 * On the left side, expand the size of the contactList so that it uses
		 * all the space left after from bottomLeftLayout
		 */
		leftLayout.setExpandRatio(contactList, 1);
		contactList.setSizeFull();

		/*
		 * In the bottomLeftLayout, searchField takes all the width there is
		 * after adding addNewContactButton. The height of the layout is defined
		 * by the tallest component.
		 */
		bottomLeftLayout.setWidth("100%");
		searchField.setWidth("100%");
		bottomLeftLayout.setExpandRatio(searchField, 1);

		/* Put a little margin around the fields in the right side editor */
		editorLayout.setMargin(true);   //设置右侧的编辑框外部间距
		editorLayout.setVisible(false); //设置可见
	}

	/**
	 * 右侧编辑框
	 */
	private void initEditor() {

		editorLayout.addComponent(removeContactButton);

		//用户界面可以冬天的 显示底层数据
		/* User interface can be created dynamically to reflect underlying data. */
		for (String fieldName : fieldNames) {
			//每循环一次，创建一个 输入框，并且把这些输入框添加到 表单布局中 FormLabel
			TextField field = new TextField(fieldName);
			editorLayout.addComponent(field);
			field.setWidth("100%");

			/*
			 * 使用 FieldGroup 把多个组件跟数据绑定在一起
			 * 
			 * We use a FieldGroup to connect multiple components to a data
			 * source at once.
			 */
			editorFields.bind(field, fieldName);
		}

		/*
		 * 数据会被缓存在页面上，这里 选择不刷新 FieldGroup 中的数据
		 * Data can be buffered in the user interface. When doing so, commit()
		 * writes the changes to the data source. Here we choose to write the
		 * changes automatically without calling commit().
		 */
		editorFields.setBuffered(false);
	}

	private void initSearch() {

		/*
		 *  等价于 placeholder 提示
		 * We want to show a subtle prompt in the search field. We could also
		 * set a caption that would be shown above the field or description to
		 * be shown in a tooltip.
		 */
		searchField.setInputPrompt("Search contacts");

		/*
		 * 设置为 输入框 内容改变，发出 Ajax 请求的方式。这里设置为 文本改变后，延迟执行的模式
		 * Granularity for sending events over the wire can be controlled. By
		 * default simple changes like writing a text in TextField are sent to
		 * server with the next Ajax call. You can set your component to be
		 * immediate to send the changes to server immediately after focus
		 * leaves the field. Here we choose to send the text over the wire as
		 * soon as user stops writing for a moment.
		 */
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);

		/*
		 * 当文本框里面内容改变后，这里相当于发出 ajax 请求
		 * When the event happens, we handle it in the anonymous inner class.
		 * You may choose to use separate controllers (in MVC) or presenters (in
		 * MVP) instead. In the end, the preferred application architecture is
		 * up to you.
		 */
		searchField.addTextChangeListener(new TextChangeListener() {
			public void textChange(final TextChangeEvent event) {

				/* Reset the filter for the contactContainer. */
				//重置 内容面板中的过滤器
				contactContainer.removeAllContainerFilters();
				contactContainer.addContainerFilter(new ContactFilter(event
						.getText()));
			}
		});
	}

	/*
	 * 根据姓名 或 公司进行过滤的自定义过滤器
	 * 
	 * A custom filter for searching names and companies in the
	 * contactContainer.
	 */
	private class ContactFilter implements Filter {
		private String needle;

		public ContactFilter(String needle) {
			this.needle = needle.toLowerCase();
		}

		public boolean passesFilter(Object itemId, Item item) {
			String haystack = ("" + item.getItemProperty(FNAME).getValue()
					+ item.getItemProperty(LNAME).getValue() + item
					.getItemProperty(COMPANY).getValue()).toLowerCase();
			return haystack.contains(needle);
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	
	/**
	 * 新建按钮。
	 * 1、获取输入框中的内容
	 * 2、把内容添加到 表格的数据模型中
	 * 3、刷新表格，让表格显示刚刚添加的数据
	 */
	private void initAddRemoveButtons() {
		addNewContactButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {

				/*
				 * Rows in the Container data model are called Item. Here we add
				 * a new row in the beginning of the list.
				 */
				contactContainer.removeAllContainerFilters();
				Object contactId = contactContainer.addItemAt(0);  //默认插入到表格第一行

				/*
				 * Each Item has a set of Properties that hold values. Here we
				 * set a couple of those.
				 */
				contactList.getContainerProperty(contactId, FNAME).setValue(
						"刘");
				contactList.getContainerProperty(contactId, LNAME).setValue(
						"内容");

				/* Lets choose the newly created contact to edit it. */
				//每增加一行，默认选中这一行，右侧 出现这条数据的编辑 页面
				contactList.select(contactId);
			}
		});

		//删除 按钮监听器
		removeContactButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				Object contactId = contactList.getValue();
				contactList.removeItem(contactId);
			}
		});
	}

	
	//初始化表格
	private void initContactList() {
		contactList.setContainerDataSource(contactContainer);  //往表格中添加 数据 Container
		contactList.setVisibleColumns(new String[] { FNAME, LNAME, COMPANY }); //设置表格头部名称
		contactList.setSelectable(true);
		contactList.setImmediate(true);

		contactList.addValueChangeListener(new Property.ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				Object contactId = contactList.getValue();  //得到所选中行的 对象

				/*
				 * When a contact is selected from the list, we want to show
				 * that in our editor on the right. This is nicely done by the
				 * FieldGroup that binds all the fields to the corresponding
				 * Properties in our contact at once.
				 */
				if (contactId != null)
					editorFields.setItemDataSource(contactList
							.getItem(contactId));
				
				editorLayout.setVisible(contactId != null);
			}
		});
	}

	/*
	 * 创建很多虚拟的数据用来显示，在真实程序中，这里使用 SQLContainer   JPAContainer  或者其他
	 * 
	 */
	private static IndexedContainer createDummyDatasource() {
		IndexedContainer ic = new IndexedContainer();

		for (String p : fieldNames) {
			ic.addContainerProperty(p, String.class, "");
		}

		/* Create dummy data by randomly combining first and last names */
		String[] fnames = { "Peter", "Alice", "Joshua", "Mike", "Olivia",
				"Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
				"Lisa", "Marge" };
		String[] lnames = { "Smith", "Gordon", "Simpson", "Brown", "Clavel",
				"Simons", "Verne", "Scott", "Allison", "Gates", "Rowling",
				"Barks", "Ross", "Schneider", "Tate" };
		for (int i = 0; i < 1000; i++) {
			Object id = ic.addItem();
			ic.getContainerProperty(id, FNAME).setValue(
					fnames[(int) (fnames.length * Math.random())]);
			ic.getContainerProperty(id, LNAME).setValue(
					lnames[(int) (lnames.length * Math.random())]);
		}

		return ic;
	}

}
