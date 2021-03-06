/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

/**
 * 2007, Modified by Posterita Ltd.
 */

package com.ghintech.location;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.adempiere.util.Callback;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.window.FDialog;
import org.adempiere.webui.window.WAutoCompleterCity;
import org.compiere.model.GridField;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCountry;
import com.ghintech.model.LVE_MLocation;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MRegion;

import com.ghintech.model.MMunicipality;
import com.ghintech.model.MParish;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.South;
import org.zkoss.zul.Vbox;

/**
 * @author Sendy Yagambrum
 * @date July 16, 2007
 * Location Dialog Box
 * This class is based upon VLocationDialog, written by Jorg Janke
 * @author Cristina Ghita, www.arhipac.ro
 * 			<li>FR [ 2794312 ] Location AutoComplete
 * @author Teo Sarca, teo.sarca@gmail.com
 * 			<li>BF [ 2995212 ] NPE on Location dialog
 * 				https://sourceforge.net/tracker/?func=detail&aid=2995212&group_id=176962&atid=955896
 * 
 * @TODO: Implement fOnline button present in swing client
 *
 * @contributors - Carlos Ruiz / globalqss
 * 				 - Show GoogleMap on Location Dialog (integrate approach from LBR)	
 * 				 - http://jira.idempiere.com/browse/IDEMPIERE-147
 **/
public class VEWLocationDialog extends Window implements EventListener<Event>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8511642461845783366L;
	private static final String LABEL_STYLE = "white-space: nowrap;";
	/** Logger          */
	private static CLogger log = CLogger.getCLogger(VEWLocationDialog.class);
	private Label lblAddress1;
	private Label lblAddress2;
	private Label lblAddress3;
	private Label lblAddress4;
	private Label lblCity;
	private Label lblZip;
	private Label lblRegion;
	private Label lblPostal;
	private Label lblPostalAdd;
	private Label lblCountry;
	//
	private Label lblMunicipality;
	private Label lblParish;
	//fin

	private Textbox txtAddress1;
	private Textbox txtAddress2;
	private Textbox txtAddress3;
	private Textbox txtAddress4;
	private WAutoCompleterCity txtCity;
	private Textbox txtPostal;
	private Textbox txtPostalAdd;
	private Listbox lstRegion;
	private Listbox lstCountry;
	//
	private Listbox lstMunicipality;
	private Listbox lstParish;
	//fin

	private ConfirmPanel confirmPanel;
	private Grid mainPanel;

	private boolean     m_change = false;
	private LVE_MLocation   m_location;
	private int         m_origCountry_ID;
	//
	private int         m_origRegion_ID;
	private int         m_origMunicipality_ID;
	//fin
	private int         s_oldCountry_ID = 0;
	private int         s_oldRegion_ID = 0;
	private int         s_oldMunicipality_ID = 0;

	private int m_WindowNo = 0;

	private boolean isCityMandatory = false;
	private boolean isRegionMandatory = false;
	private boolean isAddress1Mandatory = false;
	private boolean isAddress2Mandatory = false;
	private boolean isAddress3Mandatory = false;
	private boolean isAddress4Mandatory = false;
	private boolean isPostalMandatory = false;
	private boolean isPostalAddMandatory = false;
	//
	private boolean isMunicipalityMandatory =false;
	private boolean isParishMandatory =false;
	//fin

	private boolean inCountryAction;
	private boolean inOKAction;

	/** The "route" key  */
	private static final String TO_ROUTE = Msg.getMsg(Env.getCtx(), "Route");
	/** The "to link" key  */
	private static final String TO_LINK = Msg.getMsg(Env.getCtx(), "Map");

	private Button toLink;
	private Button toRoute;
	private GridField m_GridField = null;
	private boolean onSaveError = false;
	//END

	public VEWLocationDialog(String title, LVE_MLocation location)
	{
		this (title, location, null);
	}

	public VEWLocationDialog(String title, LVE_MLocation location, GridField gridField) {
		
		m_GridField  = gridField;
		m_location = location;
		if (m_location == null)
			m_location = new LVE_MLocation (Env.getCtx(), 0, null);
		//  Overwrite title 
		if (m_location.getC_Location_ID() == 0)
			setTitle(Msg.getMsg(Env.getCtx(), "LocationNew"));
		else
			setTitle(Msg.getMsg(Env.getCtx(), "LocationUpdate"));    
		//
		// Reset TAB_INFO context
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Region_ID", null);
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Country_ID", null);
		//
		initComponents();
		init();
		//      Current Country
		for (MCountry country:MCountry.getCountries(Env.getCtx()))
		{
			lstCountry.appendItem(country.toString(), country);
		}
		setCountry();
		lstCountry.addEventListener(Events.ON_SELECT,this);
		lstRegion.addEventListener(Events.ON_SELECT,this);
		lstMunicipality.addEventListener(Events.ON_SELECT,this);
		lstParish.addEventListener(Events.ON_SELECT,this);
		
		m_origCountry_ID = m_location.getC_Country_ID();
		//  Current Region
		lstRegion.appendItem("", null);
		
		for (MRegion region : MRegion.getRegions(Env.getCtx(), m_origCountry_ID))
		{
			lstRegion.appendItem(region.getName(),region);
		}
		
		if (m_location.getCountry().isHasRegion()) {
			if (m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName) != null
					&& m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName).trim().length() > 0)
				lblRegion.setValue(m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName));
			else
				lblRegion.setValue(Msg.getMsg(Env.getCtx(), "Region"));
		}

		setRegion();
		m_origRegion_ID = m_location.getC_Region_ID();
		// 
		lstMunicipality.appendItem("", null); 
		for (MMunicipality municipality : MMunicipality.getMunicipalitys(Env.getCtx(), m_origRegion_ID))
		{
			lstMunicipality.appendItem(municipality.getName(),municipality);
		}
		
		setMunicipality();
		
		m_origMunicipality_ID =  m_location.getC_Municipality_ID();
		
		lstParish.appendItem("", null); 
		for (MParish parish : MParish.getParishs(Env.getCtx(), m_origMunicipality_ID,m_origRegion_ID))
		{
			lstParish.appendItem(parish.getName(),parish);
		}
		setParish();
		//fin
		initLocation();
		//               
		this.setWidth("350px");
		this.setHeight("360px"); // required fixed height for ZK to auto adjust the position based on available space
		this.setSclass("popup-dialog");
		this.setClosable(true);
		this.setBorder("normal");
		this.setShadow(true);
		this.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
	}

	private void initComponents()
	{
		lblAddress1     = new Label(Msg.getElement(Env.getCtx(), "Address1"));
		lblAddress1.setStyle(LABEL_STYLE);
		lblAddress2     = new Label(Msg.getElement(Env.getCtx(), "Address2"));
		lblAddress2.setStyle(LABEL_STYLE);
		lblAddress3     = new Label(Msg.getElement(Env.getCtx(), "Address3"));
		lblAddress3.setStyle(LABEL_STYLE);
		lblAddress4     = new Label(Msg.getElement(Env.getCtx(), "Address4"));
		lblAddress4.setStyle(LABEL_STYLE);
		lblCity         = new Label(Msg.getMsg(Env.getCtx(), "City"));
		lblCity.setStyle(LABEL_STYLE);
		lblZip          = new Label(Msg.getMsg(Env.getCtx(), "Postal"));
		lblZip.setStyle(LABEL_STYLE);
		lblRegion       = new Label(Msg.getMsg(Env.getCtx(), "Region"));
		lblRegion.setStyle(LABEL_STYLE);
		lblPostal       = new Label(Msg.getMsg(Env.getCtx(), "Postal"));
		lblPostal.setStyle(LABEL_STYLE);
		lblPostalAdd    = new Label(Msg.getMsg(Env.getCtx(), "PostalAdd"));
		lblPostalAdd.setStyle(LABEL_STYLE);
		lblCountry      = new Label(Msg.getMsg(Env.getCtx(), "Country"));
		lblCountry.setStyle(LABEL_STYLE);
		//
		lblMunicipality = new Label(Msg.translate(Env.getCtx(), "c_municipality_ID"));
		lblMunicipality.setStyle(LABEL_STYLE);
		lblParish = new Label(Msg.translate(Env.getCtx(), "c_parish_ID"));
		lblParish.setStyle(LABEL_STYLE);
		//fin
		
		txtAddress1 = new Textbox();
		txtAddress1.setCols(20);
		txtAddress2 = new Textbox();
		txtAddress2.setCols(20);
		txtAddress3 = new Textbox();
		txtAddress3.setCols(20);
		txtAddress4 = new Textbox();
		txtAddress4.setCols(20);

		//autocomplete City
		txtCity = new WAutoCompleterCity(m_WindowNo);
		txtCity.setCols(20);
		txtCity.setAutodrop(true);
		txtCity.setAutocomplete(true);
		txtCity.addEventListener(Events.ON_CHANGING, this);
		//txtCity

		txtPostal = new Textbox();
		txtPostal.setCols(20);
		txtPostalAdd = new Textbox();
		txtPostalAdd.setCols(20);

		lstRegion    = new Listbox();
		lstRegion.setMold("select");
		lstRegion.setWidth("154px");
		lstRegion.setRows(0);

		lstCountry  = new Listbox();
		lstCountry.setMold("select");
		lstCountry.setWidth("154px");
		lstCountry.setRows(0);

		//
		lstMunicipality  = new Listbox();
		lstMunicipality.setMold("select");
		lstMunicipality.setWidth("154px");
		lstMunicipality.setRows(0);
		lstParish  = new Listbox();
		lstParish.setMold("select");
		lstParish.setWidth("154px");
		lstParish.setRows(0);
		//fin
		
		confirmPanel = new ConfirmPanel(true);
		confirmPanel.addActionListener(this);

		toLink = new Button(TO_LINK);
		LayoutUtils.addSclass("txt-btn", toLink);
		toLink.addEventListener(Events.ON_CLICK,this);
		toRoute = new Button(TO_ROUTE);
		LayoutUtils.addSclass("txt-btn", toRoute);
		toRoute.addEventListener(Events.ON_CLICK,this);

		mainPanel = GridFactory.newGridLayout();
	}

	private void init()
	{
		Columns columns = new Columns();
		mainPanel.appendChild(columns);
		
		Column column = new Column();
		columns.appendChild(column);
		column.setWidth("30%");
		
		column = new Column();
		columns.appendChild(column);
		column.setWidth("70%");
		
		Row pnlAddress1 = new Row();
		pnlAddress1.appendChild(lblAddress1.rightAlign());
		pnlAddress1.appendChild(txtAddress1);
		txtAddress1.setHflex("1");

		Row pnlAddress2 = new Row();
		pnlAddress2.appendChild(lblAddress2.rightAlign());
		pnlAddress2.appendChild(txtAddress2);
		txtAddress2.setHflex("1");

		Row pnlAddress3 = new Row();
		pnlAddress3.appendChild(lblAddress3.rightAlign());
		pnlAddress3.appendChild(txtAddress3);
		txtAddress3.setHflex("1");

		Row pnlAddress4 = new Row();
		pnlAddress4.appendChild(lblAddress4.rightAlign());
		pnlAddress4.appendChild(txtAddress4);
		txtAddress4.setHflex("1");

		Row pnlCity     = new Row();
		pnlCity.appendChild(lblCity.rightAlign());
		pnlCity.appendChild(txtCity);
		txtCity.setHflex("1");

		Row pnlPostal   = new Row();
		pnlPostal.appendChild(lblPostal.rightAlign());
		pnlPostal.appendChild(txtPostal);
		txtPostal.setHflex("1");

		Row pnlPostalAdd = new Row();
		pnlPostalAdd.appendChild(lblPostalAdd.rightAlign());
		pnlPostalAdd.appendChild(txtPostalAdd);
		txtPostalAdd.setHflex("1");

		Row pnlRegion    = new Row();
		pnlRegion.appendChild(lblRegion.rightAlign());
		pnlRegion.appendChild(lstRegion);
		lstRegion.setHflex("1");

		Row pnlCountry  = new Row();
		pnlCountry.appendChild(lblCountry.rightAlign());
		pnlCountry.appendChild(lstCountry);
		
		//
		Row pnlMunicipality  = new Row();
		pnlMunicipality.appendChild(lblMunicipality.rightAlign());
		pnlMunicipality.appendChild(lstMunicipality);

		
		Row pnlParish  = new Row();
		pnlParish.appendChild(lblParish.rightAlign());
		pnlParish.appendChild(lstParish);
		//fin
lstCountry.setHflex("1");
		Panel pnlLinks    = new Panel();
		pnlLinks.appendChild(toLink);
		if (LVE_MLocation.LOCATION_MAPS_URL_PREFIX == null)
			toLink.setVisible(false);
		pnlLinks.appendChild(toRoute);
		if (LVE_MLocation.LOCATION_MAPS_ROUTE_PREFIX == null || Env.getAD_Org_ID(Env.getCtx()) <= 0)
			toRoute.setVisible(false);
		pnlLinks.setWidth("100%");
		pnlLinks.setStyle("text-align:right");
		
		Borderlayout borderlayout = new Borderlayout();
		this.appendChild(borderlayout);
		borderlayout.setHflex("1");
		borderlayout.setVflex("1");
		
		Center centerPane = new Center();
		centerPane.setSclass("dialog-content");
		centerPane.setAutoscroll(true);
		borderlayout.appendChild(centerPane);
		
		Vbox vbox = new Vbox();
		centerPane.appendChild(vbox);
		vbox.appendChild(mainPanel);
		if (LVE_MLocation.LOCATION_MAPS_URL_PREFIX != null || LVE_MLocation.LOCATION_MAPS_ROUTE_PREFIX != null)
			vbox.appendChild(pnlLinks);
		vbox.setVflex("1");
		vbox.setHflex("1");

		South southPane = new South();
		southPane.setSclass("dialog-footer");
		borderlayout.appendChild(southPane);
		southPane.appendChild(confirmPanel);
		
		addEventListener("onSaveError", this);
	}
	/**
	 * Dynamically add fields to the Location dialog box
	 * @param panel panel to add
	 *
	 */
	private void addComponents(Row row)
	{
		if (mainPanel.getRows() != null)
			mainPanel.getRows().appendChild(row);
		else
			mainPanel.newRows().appendChild(row);
	}

	private void initLocation()
	{
		if (mainPanel.getRows() != null)
			mainPanel.getRows().getChildren().clear();

		MCountry country = m_location.getCountry();
		if (log.isLoggable(Level.FINE)) log.fine(country.getName() + ", Region=" + country.isHasRegion() + " " + country.getCaptureSequence()
				+ ", C_Location_ID=" + m_location.getC_Location_ID());
		//  new Country
		if (m_location.getC_Country_ID() != s_oldCountry_ID)
		{
			lstRegion.getChildren().clear();
			if (country.isHasRegion()) {
				lstRegion.appendItem("", null);
				for (MRegion region : MRegion.getRegions(Env.getCtx(), country.getC_Country_ID()))
				{
					lstRegion.appendItem(region.getName(),region);
				}
				if (m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName) != null
						&& m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName).trim().length() > 0)
					lblRegion.setValue(m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName));
				else
					lblRegion.setValue(Msg.getMsg(Env.getCtx(), "Region"));
			}
			s_oldCountry_ID = m_location.getC_Country_ID();
		}
		
		if (m_location.getC_Region_ID() > 0 && m_location.getC_Region().getC_Country_ID() == country.getC_Country_ID()) {
			setRegion();
		} else {
			lstRegion.setSelectedItem(null);
			m_location.setC_Region_ID(0);
		}

		if (country.isHasRegion() && m_location.getC_Region_ID() > 0)
		{
			Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Region_ID", String.valueOf(m_location.getC_Region_ID()));
		} else {
			Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Region_ID", "0");
		}
		Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Country_ID", String.valueOf(country.get_ID()));
		
		txtCity.fillList();
				
		// actualiza cuando cambia la region
		//MRegion region= m_location.getRegion();
		
		if (m_location.getC_Region_ID() != s_oldRegion_ID)
		{
			
			lstMunicipality.getChildren().clear();
			lstMunicipality.appendItem("", null);
			lstParish.getChildren().clear();
			lstParish.appendItem("", null); 
			
			 
			for (MMunicipality municipality : MMunicipality.getMunicipalitys(Env.getCtx(), m_location.getC_Region_ID()))
			{
				lstMunicipality.appendItem(municipality.getName(),municipality);
				
			}
			//m_origMunicipality_ID =  m_location.getC_Municipality_ID();
			if(m_location.getC_Municipality_ID()>0){
				setMunicipality();
			} else {
				lstMunicipality.setSelectedItem(null);
				m_location.setC_Municipality_ID(0);
			}
			
			s_oldRegion_ID = m_location.getC_Region_ID();
		
			if (m_location.getC_Municipality_ID() > 0)
			{
				Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Municipality_ID", String.valueOf(m_location.getC_Municipality_ID()));
			} else {
				Env.setContext(Env.getCtx(), m_WindowNo, Env.TAB_INFO, "C_Municipality_ID", "0");
			}
		}
		
		if (m_location.getC_Municipality_ID() != s_oldMunicipality_ID)
		{
			//MMunicipality municipality =m_location.getMunicipality();
			
			lstParish.getChildren().clear();
		
			lstParish.appendItem("", null); 
			for (MParish parish : MParish.getParishs(Env.getCtx(),  m_location.getC_Municipality_ID(),m_location.getC_Region_ID()))
			{
				lstParish.appendItem(parish.getName(),parish);
			}
			setParish();
			s_oldMunicipality_ID = m_location.getC_Municipality_ID();
		}
		
		
		
		//      sequence of City Postal Region - @P@ @C@ - @C@, @R@ @P@
		String ds = country.getCaptureSequence();
		if (ds == null || ds.length() == 0)
		{
			log.log(Level.SEVERE, "CaptureSequence empty - " + country);
			ds = "";    //  @C@,  @P@
		}
		isCityMandatory = false;
		isRegionMandatory = false;
		isAddress1Mandatory = false;
		isAddress2Mandatory = false;
		isAddress3Mandatory = false;
		isAddress4Mandatory = false;
		isPostalMandatory = false;
		isPostalAddMandatory = false;
		//
		isMunicipalityMandatory = false;
		isParishMandatory = false;
		//fin
		
		StringTokenizer st = new StringTokenizer(ds, "@", false);
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			if (s.startsWith("CO")) {
				//  Country Last
				addComponents((Row)lstCountry.getParent());
				// TODO: Add Online
				// if (m_location.getCountry().isPostcodeLookup()) {
					// addLine(line++, lOnline, fOnline);
				// }
			} else if (s.startsWith("A1")) {
				addComponents((Row)txtAddress1.getParent());
				isAddress1Mandatory = s.endsWith("!");
			} else if (s.startsWith("A2")) {
				addComponents((Row)txtAddress2.getParent());
				isAddress2Mandatory = s.endsWith("!");
			} else if (s.startsWith("A3")) {
				addComponents((Row)txtAddress3.getParent());
				isAddress3Mandatory = s.endsWith("!");
			} else if (s.startsWith("A4")) {
				addComponents((Row)txtAddress4.getParent());
				isAddress4Mandatory = s.endsWith("!");
			} else if (s.startsWith("C")) {
				addComponents((Row)txtCity.getParent());
				isCityMandatory = s.endsWith("!");
			//
			}else if (s.startsWith("MU") ) {
				addComponents((Row)lstMunicipality.getParent());
				isMunicipalityMandatory = s.endsWith("!");
			}else if (s.startsWith("PA") ) {
				addComponents((Row)lstParish.getParent());
				isParishMandatory = s.endsWith("!");
			}else if (s.startsWith("P")) {
				addComponents((Row)txtPostal.getParent());
				isPostalMandatory = s.endsWith("!");
			} else if (s.startsWith("A")) {
				addComponents((Row)txtPostalAdd.getParent());
				isPostalAddMandatory = s.endsWith("!");
			} else if (s.startsWith("R") && m_location.getCountry().isHasRegion()) {
				addComponents((Row)lstRegion.getParent());
				isRegionMandatory = s.endsWith("!");
			}
		}

		//      Fill it
		if (m_location.getC_Location_ID() != 0)
		{
			txtAddress1.setText(m_location.getAddress1());
			txtAddress2.setText(m_location.getAddress2());
			txtAddress3.setText(m_location.getAddress3());
			txtAddress4.setText(m_location.getAddress4());
			txtCity.setText(m_location.getCity());
			txtPostal.setText(m_location.getPostal());
			txtPostalAdd.setText(m_location.getPostal_Add());
			
			setParish();
			setMunicipality();
			if (m_location.getCountry().isHasRegion())
			{
				if (m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName) != null
						&& m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName).trim().length() > 0)
					lblRegion.setValue(m_location.getCountry().get_Translation(MCountry.COLUMNNAME_RegionName));
				else
					lblRegion.setValue(Msg.getMsg(Env.getCtx(), "Region"));

				setRegion();                
			}
			setCountry();
		}
	}
	private void setCountry()
	{
		List<?> listCountry = lstCountry.getChildren();
		Iterator<?> iter = listCountry.iterator();
		while (iter.hasNext())
		{
			ListItem listitem = (ListItem)iter.next();
			if (m_location.getCountry().equals(listitem.getValue()))
			{
				lstCountry.setSelectedItem(listitem);
			}
		}
	}

	private void setRegion()
	{
		if (m_location.getRegion() != null) 
		{
			List<?> listState = lstRegion.getChildren();
			Iterator<?> iter = listState.iterator();
			while (iter.hasNext())
			{
				ListItem listitem = (ListItem)iter.next();
				if (m_location.getRegion().equals(listitem.getValue()))
				{
					lstRegion.setSelectedItem(listitem);
				}
			}
		}
		else
		{
			lstRegion.setSelectedItem(null);
		}        
	}
	//
	private void setMunicipality()
	{
		if (m_location.getMunicipality() != null) 
		{
			List<?> listState = lstMunicipality.getChildren();
			Iterator<?> iter = listState.iterator();
			while (iter.hasNext())
			{
				ListItem listitem = (ListItem)iter.next();
				if (m_location.getMunicipality().equals(listitem.getValue()))
				{
					lstMunicipality.setSelectedItem(listitem);
				}
			}
		}
		else
		{
			lstMunicipality.setSelectedItem(null);
		}        
	}
	private void setParish()
	{
		if (m_location.getParish() != null) 
		{
			List<?> listState = lstParish.getChildren();
			Iterator<?> iter = listState.iterator();
			while (iter.hasNext())
			{
				ListItem listitem = (ListItem)iter.next();
				if (m_location.getParish().equals(listitem.getValue()))
				{
					lstParish.setSelectedItem(listitem);
				}
			}
		}
		else
		{
			lstParish.setSelectedItem(null);
		}        
	}
	//fin
	/**
	 *  Get result
	 *  @return true, if changed
	 */
	public boolean isChanged()
	{
		return m_change;
	}   //  getChange
	/**
	 *  Get edited Value (MLocation)
	 *  @return location
	 */
	public LVE_MLocation getValue()
	{
		return m_location;
	}   

	public void onEvent(Event event) throws Exception
	{
		if (event.getTarget() == confirmPanel.getButton(ConfirmPanel.A_OK)) 
		{
			onSaveError = false;
			
			inOKAction = true;
			
			if (m_location.getCountry().isHasRegion() && lstRegion.getSelectedItem() == null) {
				if (txtCity.getC_Region_ID() > 0 && txtCity.getC_Region_ID() != m_location.getC_Region_ID()) {
					m_location.setRegion(MRegion.get(Env.getCtx(), txtCity.getC_Region_ID()));
					setRegion();
				}
			}
			if(m_location.getC_Country_ID()==339){
				if(lstMunicipality.getSelectedItem().getValue()==null ){
					m_location.setC_Municipality_ID(0);
				}
				if(lstParish.getSelectedItem().getValue()==null ){
					m_location.setC_Parish_ID(0);
				}
			}
			String msg = validate_OK();
			if (msg != null) {
				onSaveError = true;
				FDialog.error(0, this, "FillMandatory", Msg.parseTranslation(Env.getCtx(), msg), new Callback<Integer>() {					
					@Override
					public void onCallback(Integer result) {
						Events.echoEvent("onSaveError", VEWLocationDialog.this, null);
					}
				});
				inOKAction = false;
				return;
			}
			
			if (action_OK())
			{
				m_change = true;
				inOKAction = false;
				this.dispose();
			}
			else
			{
				onSaveError = true;
				FDialog.error(0, this, "CityNotFound", (String)null, new Callback<Integer>() {					
					@Override
					public void onCallback(Integer result) {
						Events.echoEvent("onSaveError", VEWLocationDialog.this, null);
					}
				});
			}
			inOKAction = false;
		}
		else if (event.getTarget() == confirmPanel.getButton(ConfirmPanel.A_CANCEL))
		{
			m_change = false;
			this.dispose();
		}
		else if (toLink.equals(event.getTarget()))
		{
			String urlString = LVE_MLocation.LOCATION_MAPS_URL_PREFIX + getFullAdress();
			String message = null;
			try {
				Executions.getCurrent().sendRedirect(urlString, "_blank");
			}
			catch (Exception e) {
				message = e.getMessage();
				FDialog.warn(0, this, "URLnotValid", message);
			}
		}
		else if (toRoute.equals(event.getTarget()))
		{
			int AD_Org_ID = Env.getAD_Org_ID(Env.getCtx());
			if (AD_Org_ID != 0){
				MOrgInfo orgInfo = 	MOrgInfo.get(Env.getCtx(), AD_Org_ID,null);
				LVE_MLocation orgLocation = new LVE_MLocation(Env.getCtx(),orgInfo.getC_Location_ID(),null);

				String urlString = LVE_MLocation.LOCATION_MAPS_ROUTE_PREFIX +
						LVE_MLocation.LOCATION_MAPS_SOURCE_ADDRESS + orgLocation.getMapsLocation() + //org
						         LVE_MLocation.LOCATION_MAPS_DESTINATION_ADDRESS + getFullAdress(); //partner
				String message = null;
				try {
					Executions.getCurrent().sendRedirect(urlString, "_blank");
				}
				catch (Exception e) {
					message = e.getMessage();
					FDialog.warn(0, this, "URLnotValid", message);
				}
			}
		}
		//  Country Changed - display in new Format
		else if (lstCountry.equals(event.getTarget()))
		{
			inCountryAction = true;
			MCountry c = (MCountry)lstCountry.getSelectedItem().getValue();
			m_location.setCountry(c);
			m_location.setC_City_ID(0);
			m_location.setCity(null);
			//  refresh
			initLocation();
			inCountryAction = false;
			lstCountry.focus();
		}
		//  Region Changed 
		else if (lstRegion.equals(event.getTarget()))
		{
			if (inCountryAction || inOKAction)
				return;
			MRegion r = (MRegion)lstRegion.getSelectedItem().getValue();
			m_location.setRegion(r);
			m_location.setC_City_ID(0);
			m_location.setCity(null);
			//  refresh
			initLocation();
			lstRegion.focus();
		}
		else if (lstMunicipality.equals(event.getTarget()))
		{
			if (inCountryAction || inOKAction)
				return;
			
			MRegion r = (MRegion)lstRegion.getSelectedItem().getValue();
			m_location.setRegion(r);
			m_location.setC_City_ID(0);
			m_location.setCity(null);
			MMunicipality m = (MMunicipality)lstMunicipality.getSelectedItem().getValue();
			m_location.setMunicipality(m);
			
			initLocation();
			//lstMunicipality.focus();
		}
	}

	
	// LCO - address 1, region and city required
	private String validate_OK() {
		String fields = "";
		if (isAddress1Mandatory && txtAddress1.getText().trim().length() == 0) {
			fields = fields + " " + "@Address1@, ";
		}
		if (isAddress2Mandatory && txtAddress2.getText().trim().length() == 0) {
			fields = fields + " " + "@Address2@, ";
		}
		if (isAddress3Mandatory && txtAddress3.getText().trim().length() == 0) {
			fields = fields + " " + "@Address3@, ";
		}
		if (isAddress4Mandatory && txtAddress4.getText().trim().length() == 0) {
			fields = fields + " " + "@Address4@, ";
		}
		if (isCityMandatory && txtCity.getValue().trim().length() == 0) {
			fields = fields + " " + "@C_City_ID@, ";
		}
		if (isRegionMandatory && lstRegion.getSelectedItem() == null) {
			fields = fields + " " + "@C_Region_ID@, ";
		}
		if (isPostalMandatory && txtPostal.getText().trim().length() == 0) {
			fields = fields + " " + "@Postal@, ";
		}
		if (isPostalAddMandatory && txtPostalAdd.getText().trim().length() == 0) {
			fields = fields + " " + "@PostalAdd@, ";
		}
		if(m_location.getC_Country_ID()==339){
		//
			if (isMunicipalityMandatory && lstMunicipality.getSelectedItem() == null) {
				fields = fields + " " + "@C_Municipality_ID@, ";
			}
			if (isParishMandatory && lstParish.getSelectedItem() == null) {
				fields = fields + " " + "@C_parish_ID@, ";
			}
			//fin
		}
		if (fields.trim().length() > 0)
			return fields.substring(0, fields.length() -2);

		return null;
	}

	/**
	 *  OK - check for changes (save them) & Exit
	 */
	private boolean action_OK()
	{
		Trx trx = Trx.get(Trx.createTrxName("WLocationDialog"), true);
		m_location.set_TrxName(trx.getTrxName());
		m_location.setAddress1(txtAddress1.getValue());
		m_location.setAddress2(txtAddress2.getValue());
		m_location.setAddress3(txtAddress3.getValue());
		m_location.setAddress4(txtAddress4.getValue());
		m_location.setC_City_ID(txtCity.getC_City_ID()); 
		m_location.setCity(txtCity.getValue());
		m_location.setPostal(txtPostal.getValue());
		//  Country/Region
		MCountry country = (MCountry)lstCountry.getSelectedItem().getValue();
		m_location.setCountry(country);
		if (country.isHasRegion() && lstRegion.getSelectedItem() != null)
		{
			MRegion r = (MRegion)lstRegion.getSelectedItem().getValue();
			m_location.setRegion(r);
		}
		else
		{
			m_location.setC_Region_ID(0);
		}
		if(m_location.getC_Country_ID()==339){
		//
			MMunicipality m=(MMunicipality)lstMunicipality.getSelectedItem().getValue();
			m_location.setMunicipality(m);
			MParish p=(MParish)lstParish.getSelectedItem().getValue();
			m_location.setParish(p);
		}
		//fin
		//  Save changes
		boolean success = false;
		if (m_location.save())
		{
            // IDEMPIERE-417 Force Update BPLocation.Name
        	if (m_GridField != null && m_GridField.getGridTab() != null
        			&& "C_BPartner_Location".equals(m_GridField.getGridTab().getTableName()))
    		{
        		m_GridField.getGridTab().setValue("Name", ".");
				success = true;
    		} else {
    			//Update BP_Location name IDEMPIERE 417
    			int bplID = DB.getSQLValueEx(trx.getTrxName(), "SELECT C_BPartner_Location_ID FROM C_BPartner_Location WHERE C_Location_ID = " + m_location.getC_Location_ID());
    			if (bplID>0)
    			{
    				MBPartnerLocation bpl = new MBPartnerLocation(Env.getCtx(), bplID, trx.getTrxName());
//    				bpl.setName(bpl.getBPLocName(m_location));
    				if (bpl.save())
    					success = true;
    			} else {
					success = true;
    			}
    		}
		}
		if (success) {
			trx.commit();
		} else {
			trx.rollback();
		}
		trx.close();

		return success;
	}   //  actionOK

	public boolean isOnSaveError() {
		return onSaveError;
	}
	
	@Override
	public void dispose()
	{
		if (!m_change && m_location != null && !m_location.is_new())
		{
			m_location = new LVE_MLocation(m_location.getCtx(), m_location.get_ID(), null);
		}	
		super.dispose();
	}
	
	/** returns a string that contains all fields of current form */
	String getFullAdress()
	{
		MRegion region = null;

		if (lstRegion.getSelectedItem()!=null)
			region = new MRegion(Env.getCtx(), ((MRegion)lstRegion.getSelectedItem().getValue()).getC_Region_ID(), null);
		
		MCountry c = (MCountry)lstCountry.getSelectedItem().getValue();

		String address = "";
		address = address + (txtAddress1.getText() != null ? txtAddress1.getText() + ", " : "");
		address = address + (txtAddress2.getText() != null ? txtAddress2.getText() + ", " : "");
		address = address + (txtCity.getText() != null ? txtCity.getText() + ", " : "");
		if (region != null)
			address = address + (region.getName() != null ? region.getName() + ", " : "");

		address = address + (c.getName() != null ? c.getName() : "");
		return address.replace(" ", "+");
	}	
}
