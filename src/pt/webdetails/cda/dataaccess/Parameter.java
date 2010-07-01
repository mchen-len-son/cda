package pt.webdetails.cda.dataaccess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.Formula;

import org.dom4j.Element;

import pt.webdetails.cda.utils.Util;

/**
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 4, 2010
 * Time: 4:09:48 PM
 */
public class Parameter implements java.io.Serializable
{

	private static final long serialVersionUID = 1L;
	
	private String name;
  private String type;
  private String defaultValue;
  private String pattern;
  private String stringValue;
  
  private final static String FORMULA_BEGIN = "${";
  private final static String FORMULA_END = "}";
  
  enum Type{
  	
  	STRING("String"),
  	INTEGER("Integer"),
  	NUMERIC("Numeric"),
  	DATE("Date");
  	
  	private String name;
  	
  	Type(String name){
  		this.name = name;
  	}
  	
  	public final String getName(){
  		return name;
  	}
  	
  	public String toString(){
  		return name;
  	}
  	
  	public static Type parse(String typeString) throws ParseException{
  		for(Type type : Type.values()){
  			if(type.name.equals(typeString) ){
  				return type;
  			}
  		}
  		throw new ParseException(typeString + " is not recognized by " + Type.class.getCanonicalName(),0);
  	}
  	
  	public static Type inferTypeFromObject(Object obj){
  		if(obj != null){
  			if(Double.class.isAssignableFrom(obj.getClass())){
  				return NUMERIC;
  			}
  			else if(Integer.class.isAssignableFrom(obj.getClass())){
  				return INTEGER;
  			}
  			else if(Date.class.isAssignableFrom(obj.getClass())){
  				return DATE;
  			}
  			else if(String.class.isAssignableFrom(obj.getClass())){
  				return STRING;
  			}
  		}
  		return null;//default
  	}
  	
  }

  public Parameter()
  {
  }

  public Parameter(final String name, final String type, final String defaultValue, final String pattern)
  {
    this.name = name;
    this.type = type;
    this.defaultValue = defaultValue;
    this.pattern = pattern;
  }

  public Parameter(final Element p)
  {
    this(
        p.attributeValue("name"),
        p.attributeValue("type"),
        p.attributeValue("default"),
        p.attributeValue("pattern")
    );
  }

  public Parameter(final String name, final String stringValue)
  {
    this.name = name;
    this.stringValue = stringValue;
  }


  public Object getValue() throws InvalidParameterException
  {
    // This depends on the value
    final String localValue = getStringValue() == null ? getDefaultValue() : getStringValue();

    //check if it is a formula
    if(localValue.trim().startsWith(FORMULA_BEGIN)){
    	processFormula(Util.getContentsBetween(localValue, FORMULA_BEGIN, FORMULA_END));
    }
    
    if (getType().equals("String"))
    {
      return localValue;
    }
    else if (getType().equals("Integer"))
    {
      return Integer.parseInt(localValue);
    }
    else if (getType().equals("Numeric"))
    {
      return Double.parseDouble(localValue);
    }
    else if (getType().equals("Date"))
    {
      SimpleDateFormat format = new SimpleDateFormat(getPattern());
      try
      {
        return format.parse(localValue);
      }
      catch (ParseException e)
      {
        throw new InvalidParameterException("Unable to parse date " + localValue + " with pattern " + getPattern() , e);
      }
    }
    else{
      throw  new InvalidParameterException("Parameter type " + getType() + " unknown, can't continue",null);
    }


  }


  private static Object processFormula(String localValue) throws InvalidParameterException {
  	try {
			Formula formula = new Formula(localValue);
			formula.initialize(new DefaultFormulaContext());
			return formula.evaluate();
		} catch (org.pentaho.reporting.libraries.formula.parser.ParseException e) {
			throw new InvalidParameterException("Unable to parse expression " + localValue, e);
		}
		catch(org.pentaho.reporting.libraries.formula.EvaluationException e){
			throw new InvalidParameterException("Unable to evaluate expression " + localValue, e);
		}
	}

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getType()
  {
    return type;
  }

  public void setType(final String type)
  {
    this.type = type;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  public void setDefaultValue(final String defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  public String getPattern()
  {
    return pattern;
  }

  public void setPattern(final String pattern)
  {
    this.pattern = pattern;
  }

  public String getStringValue()
  {
    return stringValue;
  }

  public void setStringValue(final String stringValue)
  {
    this.stringValue = stringValue;
  }
  
  /**
   * For debugging purposes
   */
  public String toString(){
  	return getName() + "=" + getStringValue();
  }

}
