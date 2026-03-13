package ru.ivent.platform.telegram.method;

import ru.ivent.http.EmbeddableContent;
import ru.ivent.platform.ApiMethodParams;

/**
 * @author Laughina
 */
public interface TelegramMethodParams extends ApiMethodParams {

    void set(String field, Object value);

    void setFile(String field, String filename, EmbeddableContent fileContent);

}
