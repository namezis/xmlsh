package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSELBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
import net.sf.saxon.s9api.SaxonApiException;

public class elbRegister extends AWSELBCommand {

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions();
    parseOptions(opts, args);

    args = opts.getRemainingArgs();

    setSerializeOpts(this.getSerializeOpts(opts));

    if(args.size() < 2) {
      usage();
      return -1;
    }

    String elb = args.remove(0).toString();

    try {
      getELBClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret = register(elb, args);

    return ret;

  }

  protected int register(String elb, List<XValue> args)
      throws XMLStreamException, IOException,
      SaxonApiException, CoreException {

    OutputPort stdout = this.getStdout();
    mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

    startDocument();
    startElement(getName());

    RegisterInstancesWithLoadBalancerRequest request = new RegisterInstancesWithLoadBalancerRequest(
        elb, instances(args));
    traceCall("registerInstancesWithLoadBalancer");
    RegisterInstancesWithLoadBalancerResult result = getAWSClient()
        .registerInstancesWithLoadBalancer(request);
    for(Instance instance : result.getInstances()) {

      startElement("instance");
      attribute("instance-id", instance.getInstanceId());
      endElement();

    }

    endElement();
    endDocument();
    closeWriter();
    return 0;

  }

}
