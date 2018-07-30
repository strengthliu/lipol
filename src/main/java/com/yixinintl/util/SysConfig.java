package com.yixinintl.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SysConfig {

  private static ResourceBundle Configbundle = null;

  private SysConfig() {
  }

  /**
   * 根据key到config.properties文件中获取改key的值
   * 
   * @param s
   *          String ：key
   * @return String ：value
   */
  public static String getString(String s) {
    try {
      return getResourceBundle().getString(s);
    } catch (MissingResourceException missingresourceexception) {
      missingresourceexception.printStackTrace();
    }
    return null;
  }

  /**
   * @function getResourceBundle
   * @return ResourceBundles
   */
  private static ResourceBundle getResourceBundle() {
    if (Configbundle == null)
      Configbundle = ResourceBundle.getBundle("config");
    return Configbundle;
  }

}
