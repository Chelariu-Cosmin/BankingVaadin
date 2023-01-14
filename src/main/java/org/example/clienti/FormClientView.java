package org.example.clienti;

import banking.Client;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.example.MainView;

@PageTitle("client")
@Route(value = "client", layout = MainView.class)
public class FormClientView extends VerticalLayout implements HasUrlParameter<Integer>{
    private static final long serialVersionUID = 1L;

    // Definire model date
    private EntityManager em;
    private Client client = null;
    private Binder<Client> binder = new BeanValidationBinder<>(Client.class);

    // Definire componente view
    // Definire Form-Master
    private VerticalLayout  formLayoutToolbar;
    private H1 	titluForm 	= new H1("Form Client");
    private IntegerField 	id 	= new IntegerField("ID client:");
    private TextField 		username = new TextField("username client: ");
    // Definire componente actiuni Form-Master-Controller
    private Button 			cmdAdaugare = new Button("Adauga");
    private Button 			cmdSterge 	= new Button("Sterge");
    private Button 			cmdAbandon 	= new Button("Abandon");
    private Button 			cmdSalveaza = new Button("Salveaza");

    // Start Form
    public FormClientView() {
        //
        initDataModel();
        //
        initViewLayout();
        //
        initControllerActions();
    }
    // Navigation Management
    @Override
    public void setParameter(BeforeEvent event,
                             @OptionalParameter Integer id) {
        System.out.println("Client ID: " + id);
        if (id != null) {
            // EDIT Item
            this.client = em.find(Client.class, id);
            System.out.println("Selected client to edit:: " + client);
            if (this.client == null) {
                System.out.println("ADD client:: " + client);
                // NEW Item
                this.adaugaClient();
                this.client.setId(id);
                this.client.setUsername("Client NOU " + id);
            }
        }
        this.refreshForm();
    }
    // init Data Model
    private void initDataModel(){
        System.out.println("DEBUG START FORM >>>  ");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        this.em = emf.createEntityManager();
        this.client = em
                .createQuery("SELECT c FROM Client c ORDER BY c.id", Client.class)
                .getResultStream().findFirst().get();

        //
        binder.forField(id).bind("id");
        binder.forField(username).bind("username");
        //
        refreshForm();
    }

    // init View Model
    private void initViewLayout() {
        // Form-Master-Details -----------------------------------//
        // Form-Master
        FormLayout formLayout = new FormLayout();
        formLayout.add(id, username);
        formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
        formLayout.setMaxWidth("400px");
        // Toolbar-Actions-Master
        HorizontalLayout actionToolbar =
                new HorizontalLayout(cmdAdaugare, cmdSterge, cmdAbandon, cmdSalveaza);
        actionToolbar.setPadding(false);

        //
        this.formLayoutToolbar = new VerticalLayout(formLayout, actionToolbar);
        // ---------------------------
        this.add(titluForm, formLayoutToolbar);
        //
    }

    // init Controller components
    private void initControllerActions() {
        // Transactional Master Actions
        cmdAdaugare.addClickListener(e -> {
            adaugaClient();
            refreshForm();
        });
        cmdSterge.addClickListener(e -> {
            stergeClient();
            // Navigate back to NavigableGridClienteForm
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridClientiView.class)
            );
        });
        cmdAbandon.addClickListener(e -> {
            // Navigate back to NavigableGridClienteForm
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridClientiView.class, this.client.getId())
            );
        });
        cmdSalveaza.addClickListener(e -> {
            salveazaClient();
            // refreshForm();
            // Navigate back to NavigableGridClienteForm
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridClientiView.class, this.client.getId())
            );
        });
    }
    //
    private void refreshForm() {
        System.out.println("Client curent: " + this.client);
        if (this.client != null) {
            binder.setBean(this.client);
        }
    }

    // CRUD actions
    private void adaugaClient() {
        this.client = new Client();
        this.client.setId(999);
        this.client.setUsername("Client Nou");
    }

    private void stergeClient() {
        System.out.println("To remove: " + this.client);
        if (this.em.contains(this.client)) {
            this.em.getTransaction().begin();
            this.em.remove(this.client);
            this.em.getTransaction().commit();
        }
    }

    private void salveazaClient() {
        try {
            this.em.getTransaction().begin();
            this.client = this.em.merge(this.client);
            this.em.getTransaction().commit();
            System.out.println("Client Salvat");
        } catch (Exception ex) {
            if (this.em.getTransaction().isActive())
                this.em.getTransaction().rollback();
            System.out.println("*** EntityManager Validation ex: " + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }
}

