package nl.rug.blackboard.springbatchfoldermaker.util;

import blackboard.platform.plugin.PluginLocalizationUtil;


public class BatchFolderMakerLocalizationUtil extends PluginLocalizationUtil {
	private static BatchFolderMakerLocalizationUtil INSTANCE = new BatchFolderMakerLocalizationUtil();


	public BatchFolderMakerLocalizationUtil() {
		super("UG", "BatchFolderMaker");
	}


	public static BatchFolderMakerLocalizationUtil get() {
		return INSTANCE;
	}
}
