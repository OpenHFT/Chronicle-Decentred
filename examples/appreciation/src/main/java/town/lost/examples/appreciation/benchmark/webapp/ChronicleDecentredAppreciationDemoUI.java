package town.lost.examples.appreciation.benchmark.webapp;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.*;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 *
 * @author Per Minborg
 */
@Theme("mytheme")
@Push
public final class ChronicleDecentredAppreciationDemoUI extends UI {

    private static final String STYLE_NAME = "huge";
    //private static final String STYLE_NAME = "huge borderless";
    private static final int PIXEL_SIZE = 200;


    private TextField gatewayAddress;
    private CheckBox connected;
    private NativeSelect<IntStringTuple> firstAddress;
    private TextField firstInitialValue;
    private NativeSelect<IntStringTuple> secondAddress;
    private TextField secondInitialValue;
    private TextField iterations;
    private NativeSelect<Integer> callerTheads;
    //
    private TextField firstCurrentValue;
    private TextField secondCurrentValue;
    private ProgressBar progressBar;
    private TextField elapsedTime;
    private TextField tps;
    private Button button;
    //
    private volatile Updater updater;

    /* Init method is called when application starts */
    @Override
    protected void init(VaadinRequest vaadinRequest) {

        gatewayAddress = newTextField("Gateway Address", "0.0.0.0:10000");
        connected = new CheckBox("Connected");
        firstAddress = new NativeSelect<>("First Account", IntStream.range(1, 31).mapToObj(IntStringTuple::new).collect(toList()));
        firstAddress.setWidth(PIXEL_SIZE, Unit.PIXELS);
        firstAddress.setValue(new IntStringTuple(1));
        firstInitialValue = newTextField("Initial Value", "1000000");
        secondAddress = new NativeSelect<>("Second Account", IntStream.range(1, 31).mapToObj(IntStringTuple::new).collect(toList()));
        secondAddress.setWidth(PIXEL_SIZE, Unit.PIXELS);
        secondAddress.setValue(new IntStringTuple(2));
        secondInitialValue = newTextField("Initial Value", "1000000");
        iterations = newTextField("Iterations", "30000");
        callerTheads = new NativeSelect<>("Caller Threads", IntStream.range(0, 4).map(i -> 1 << i).boxed().collect(toList()));
        callerTheads.setValue(1);

        firstCurrentValue = newReadOnlyTextField("Current Value");
        secondCurrentValue = newReadOnlyTextField("Current Value");
        progressBar = new ProgressBar();
        progressBar.setCaption("Progress");
        progressBar.setEnabled(false);
        progressBar.setWidth(PIXEL_SIZE * 3, Unit.PIXELS);
        elapsedTime = newReadOnlyTextField("Elapsed Time [ms]");
        tps = newReadOnlyTextField("TPS");


        final Label appTitle = new Label("Chronicle Decentred - Appreciation Demo");
        appTitle.setStyleName("h2");
        appTitle.setHeight(6, Unit.PIXELS);
        final Label appSubTitle = new Label("A Distributed High-Performance Ledger");
        appSubTitle.setHeight(6, Unit.PIXELS);

        final String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        final FileResource resource = new FileResource(new File(basepath + "/WEB-INF/images/cs_logo.png"));
        final Image image = new Image("", resource);
        image.setWidth(80, Unit.PIXELS);

        final VerticalLayout combined = new VerticalLayout(appTitle, appSubTitle);
/*        combined.setWidthUndefined();*/
        final HorizontalLayout headline = new HorizontalLayout(image, combined, newSeparator());


        final VerticalLayout menu = new VerticalLayout();
        menu.setSizeFull();
        menu.setWidth(PIXEL_SIZE * 3 + 100, Unit.PIXELS);

        final HorizontalLayout first = new HorizontalLayout();
        first.addComponents(firstAddress, firstInitialValue, firstCurrentValue);

        final HorizontalLayout second = new HorizontalLayout();
        second.addComponents(secondAddress, secondInitialValue, secondCurrentValue);

        button = new Button("Start Benchmark");
        button.addClickListener(this::clickListener);

        final HorizontalLayout measures = new HorizontalLayout(elapsedTime, tps);

        /* ------ FINAL ASSEMBLY ----- */
        final HorizontalLayout gwLayout = new HorizontalLayout(gatewayAddress, connected);
        gwLayout.setComponentAlignment(connected, Alignment.MIDDLE_LEFT);

        menu.addComponents(headline, newSeparator(), gwLayout, first, second, iterations, /*callerTheads,*/ newSeparator(), new HorizontalLayout(progressBar), measures, button);

        setContent(menu);
    }

    private void clickListener(Button.ClickEvent clickEvent) {
        //final Button button = clickEvent.getButton();
        final Updater u = updater;
        if (u == null) {
            connected.setValue(true);
            progressBar.setEnabled(true);
            button.setCaption("Stop Benchmark");
            updater = new Updater();
            updater.start();
        } else {
            progressBar.setEnabled(false);
            button.setCaption("Start Benchmark");
            u.shutDown();
            updater = null;
        }
    }


    private TextField newTextField(String caption, String value) {
        final TextField result = new TextField(caption, value);
        result.setStyleName(STYLE_NAME);
        result.setWidth(PIXEL_SIZE, Unit.PIXELS);
        return result;
    }

    private TextField newReadOnlyTextField(String caption) {
        final TextField result = newTextField(caption, "-");
        result.setReadOnly(true);
        return result;
    }

    private Component newSeparator() {
        final Component result = new Label("<hr />", ContentMode.HTML);
        //final Component result =  new Label("_________________________________________________________________________________");
        result.setWidth(100f, Sizeable.Unit.PERCENTAGE);
        //result.setHeight(10, Unit.PIXELS);
        result.setHeightUndefined();
        return result;
    }


    private void updateGui() {
        final Updater u = updater;
        if (u != null) {
            if (!firstAddress.getValue().equals(secondAddress.getValue())) {
                final Benchmark b = u.benchmark();
                firstCurrentValue.setValue(format(b.firstBalance()));
                secondCurrentValue.setValue(format(b.secondBalance()));
                progressBar.setValue(b.progress());
                elapsedTime.setValue(format(b.elapsedMs()));
                tps.setValue(format(b.tps()));
            }
        } else {
            progressBar.setValue(0f);
        }
        progressBar.markAsDirtyRecursive();
    }


    private String format(double val) {
        return String.format("%,.0f", val);
    }

    private String format(long val) {
        return String.format("%,d", val);
    }


    private final class Updater extends Thread {

        private final AtomicBoolean running;
        private final long selfDestructTime;
        private final Benchmark benchmark;

        private Updater() {
            super(Updater.class.getSimpleName());
            this.running = new AtomicBoolean(true);
            this.selfDestructTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);


            final String[] addrPair = gatewayAddress.getValue().split(":");
            final InetSocketAddress socketAddress = InetSocketAddress.createUnresolved(addrPair[0], Integer.parseInt(addrPair[1]));
            final int it = Integer.parseInt(iterations.getValue());
            System.out.println("Updater started with " + it + " iterations");
            this.benchmark = new Benchmark(
                socketAddress,
                firstAddress.getValue().intValue,
                secondAddress.getValue().intValue,
                Double.parseDouble(firstInitialValue.getValue()),
                Double.parseDouble(secondInitialValue.getValue()),
                it,
                System.out::println,
                connected::setValue
            );
            benchmark.start();
        }

        @Override
        public void run() {
            while (running.get() && !Thread.interrupted() && System.currentTimeMillis() < selfDestructTime && !benchmark.isCompleted()) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ChronicleDecentredAppreciationDemoUI.this.access(ChronicleDecentredAppreciationDemoUI.this::updateGui);
            }
            benchmark.shutDown();
            System.out.println("Updater completed");
            ChronicleDecentredAppreciationDemoUI.this.access(() -> {button.setCaption("Start Benchmark");});
            ChronicleDecentredAppreciationDemoUI.this.updater = null;
        }

        public Benchmark benchmark() {
            return benchmark;
        }

        private void shutDown() {
            running.set(false);
        }

    }

    private final class IntStringTuple {
        private final int intValue;
        private final String stringValue;

        public IntStringTuple(int intValue) {
            this.intValue = intValue;
            final KeyPair keyPair = new KeyPair(intValue);
            this.stringValue = DecentredUtil.toAddressString(DecentredUtil.toAddress(keyPair.publicKey));
        }

        public IntStringTuple(int intValue, String stringValue) {
            this.intValue = intValue;
            this.stringValue = stringValue;
        }

        public int intValue() {return intValue;}
        public String stringValue() { return stringValue;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntStringTuple that = (IntStringTuple) o;
            return intValue == that.intValue &&
                Objects.equals(stringValue, that.stringValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(intValue, stringValue);
        }

        @Override
        public String toString() {
            return intValue + ", " + stringValue;
        }
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = ChronicleDecentredAppreciationDemoUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

}
