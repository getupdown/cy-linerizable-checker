package cn.cy.concurrent.util;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public class PathUtil {

    public static String concatPath(String parent, String child) {

        Preconditions.checkState(StringUtils.isNotBlank(parent));
        Preconditions.checkState(StringUtils.isNotBlank(child));

        Preconditions.checkState(!child.startsWith("/"));

        if (StringUtils.equals(parent, "/")) {
            return parent + child;
        } else {
            return parent + "/" + child;
        }
    }

}
