import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.randomccnumber.RandomCCNumberGeneratorMeta;
import org.pentaho.di.trans.steps.writetolog.WriteToLogMeta;

public class TransformTest {

	private TransMeta tm;
	private int id;
	
	public static void main(String[] args) {
	  for( int i = 1; i<=1000; i++ ) {
		try {
			TransformTest ft = new TransformTest( i );
			ft.saveXML();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	  }
	}
	public TransformTest( int id ) throws Exception {
		this.id = id;
		KettleEnvironment.init();
		PluginRegistry registry = PluginRegistry.getInstance();

		tm = new TransMeta();
		tm.setName( "TransformTest Transformation " + this.id );

		// Credit Card Generator
		RandomCCNumberGeneratorMeta ccardMeta = new RandomCCNumberGeneratorMeta();
		String ccardId = registry.getPluginId( StepPluginType.class, ccardMeta );
		String ccardName = "Generate Random Credit Cards";
		StepMeta ccardStep = new StepMeta( ccardId, ccardName, ccardMeta );

		ccardMeta.setCardLengthFieldName( "Card length" );
		ccardMeta.setCardNumberFieldName( "Card number" );
		ccardMeta.setCardTypeFieldName( "Card type" );
		ccardMeta.allocate( 1 );
		ccardMeta.setFieldCCType( new String[]{ "American Express" } );
		//http://jira.pentaho.com/browse/PDI-13299
		ccardMeta.getFieldCCLength()[0] = "15";
		ccardMeta.getFieldCCSize()[0] = "10";
		tm.addStep( ccardStep );

		WriteToLogMeta writeLogMeta = new WriteToLogMeta();
		writeLogMeta.setDefault();
		String writeLogId = registry.getPluginId( StepPluginType.class, writeLogMeta );
		String writeLogName = "Write to Log";
		StepMeta writeLogStep = new StepMeta( writeLogId, writeLogName, writeLogMeta );
		tm.addStep( writeLogStep );

		// Hops
		TransHopMeta hopCCardWriteLog = new TransHopMeta( ccardStep, writeLogStep );
		tm.addTransHop( hopCCardWriteLog );

		// Make pretty for Spoon
		ccardStep.setLocation(100, 50);
		writeLogStep.setLocation(200, 50);

		ccardStep.setDraw(true);
		writeLogStep.setDraw(true);
	}

	public void saveXML() {
		Writer writer = null;
		try {
			String file_number = "000" + String.valueOf( this.id );
			file_number = file_number.substring( file_number.length() - 3, file_number.length() );
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("transform_test_" + file_number + ".ktr"), "utf-8"));
			writer.write(tm.getXML());
		} catch (IOException | KettleException ex) {
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
	}
}
