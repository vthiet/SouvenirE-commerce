package nlu.fit.web.souvenirecommerce.core.logging;

import ch.qos.logback.core.PropertyDefinerBase;

public class ProjectLogDirPropertyDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return ProjectLogPaths.resolveLogDir().toString();
    }
}
