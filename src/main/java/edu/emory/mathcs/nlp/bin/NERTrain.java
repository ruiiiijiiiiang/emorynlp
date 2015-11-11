/**
 * Copyright 2015, Emory University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.emory.mathcs.nlp.bin;

import java.io.InputStream;
import java.util.List;

import edu.emory.mathcs.nlp.common.util.BinUtils;
import edu.emory.mathcs.nlp.common.util.IOUtils;
import edu.emory.mathcs.nlp.emorynlp.component.NLPOnlineComponent;
import edu.emory.mathcs.nlp.emorynlp.component.config.NLPConfig;
import edu.emory.mathcs.nlp.emorynlp.component.feature.FeatureTemplate;
import edu.emory.mathcs.nlp.emorynlp.component.node.NLPNode;
import edu.emory.mathcs.nlp.emorynlp.component.train.NLPOnlineTrain;
import edu.emory.mathcs.nlp.emorynlp.ner.NERConfig;
import edu.emory.mathcs.nlp.emorynlp.ner.NERState;
import edu.emory.mathcs.nlp.emorynlp.ner.NERTagger;
import edu.emory.mathcs.nlp.emorynlp.ner.features.NERFeatureTemplate0;
import edu.emory.mathcs.nlp.emorynlp.ner.NERAmbiguityClassMap;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class NERTrain extends NLPOnlineTrain<NLPNode,NERState<NLPNode>>
{
	private String dbpediaConfigFile = "/home/rui/workspace/emorynlp/src/main/resources/configuration/config_dbpedia.xml";
	
	public NERTrain(String[] args)
	{
		super(args);
	}
	
	@Override
	protected NLPOnlineComponent<NLPNode,NERState<NLPNode>> createComponent(InputStream config)
	{
		return new NERTagger<>(config);
	}

	@Override
	protected void initComponent(NLPOnlineComponent<NLPNode,NERState<NLPNode>> component, List<String> inputFiles)
	{
		initComponentSingleModel(component, inputFiles);
		
		NERConfig dbpediaConfig = new NERConfig(IOUtils.createFileInputStream(dbpediaConfigFile));
		NERAmbiguityClassMap map = createAmbiguityClasseMap(dbpediaConfig, inputFiles);
		((NERTagger<NLPNode>)component).setAmbiguityClassMap(map);
	}
	
	@Override
	protected FeatureTemplate<NLPNode,NERState<NLPNode>> createFeatureTemplate()
	{
		switch (feature_template)
		{
		case  0: return new NERFeatureTemplate0<NLPNode>();
		default: throw new IllegalArgumentException("Unknown feature template: "+feature_template);
		}
	}
	
	@Override
	protected NLPNode createNode()
	{
		return new NLPNode();
	}
	
	protected NERAmbiguityClassMap createAmbiguityClasseMap(NLPConfig<NLPNode> configuration, List<String> inputFiles)
	{
		BinUtils.LOG.info("Collecting lexicons:\n");
		NERAmbiguityClassMap ac = new NERAmbiguityClassMap();
		NERConfig config = (NERConfig)configuration;
		
		iterate(configuration.getTSVReader(), inputFiles, nodes -> ac.add(nodes));
		ac.expand(config.getAmbiguityClassThreshold());
		
		BinUtils.LOG.info(String.format("- # of ambiguity classes: %d\n", ac.size()));
		return ac;
	}
	
	static public void main(String[] args)
	{
		new NERTrain(args).train();
	}
}
