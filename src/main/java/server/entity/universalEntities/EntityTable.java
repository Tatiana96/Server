package server.entity.universalEntities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanta on 27.11.2016.
 */
public class EntityTable {

    public String name;
    public List<String> columnNames;
    private List entityList;
    public Map<Integer, List<TableField> > rows;
    public List<Integer> searchOutRowsNumbers;
    public Map<Integer, List<Integer>> searchOutTableFieldsNumbers;
    public Integer searchInstancesNumber = 0;
    public List<Integer> foreignKeyFieldsNumbers;

    public EntityTable(String selectedName, List<String> selectedColumnNames, List detectedRows, Map<String, List> detectedForeignKeys) throws IllegalAccessException {
        this.name = selectedName;
        this.columnNames = selectedColumnNames;
        this.rows = new HashMap<Integer, List<TableField> >();
        this.foreignKeyFieldsNumbers = new ArrayList<Integer>();
        this.entityList = detectedRows;
        setRowsValues(detectedRows, detectedForeignKeys);
    }

    public EntityTable() {
        this.name = "";
        this.columnNames = new ArrayList<String>();
        this.entityList = new ArrayList();
        this.rows = new HashMap<Integer, List<TableField>>();
    }

    void setRowsValues(List detectedRows, Map<String, List> detectedForeignKeys) throws IllegalAccessException {

        int k = 0; // counter of amount of fields
        Integer amountOfColumns = this.columnNames.size();
        Integer sizeOfDetectedRows = detectedRows.size(); // without empty object (!NB: correct this part then)

        Object valueOfField;

        String strValue;

        Integer idValueOfForeignKey;
        String strValueOfForeignKey;

        String fieldName;
        String typeNameOfField;

        List<TableField> rowDataList;

            for (int i=0; i < sizeOfDetectedRows; i++) { // loop for objects in list

                Object tmpObject = detectedRows.get(i); // object of class, that user chose before
                Field[] fields = tmpObject.getClass().getDeclaredFields(); // fields of this class

                rowDataList = new ArrayList<TableField>();

                for (Field field : fields) { // loop for fields in object
                    if (k < amountOfColumns) { // check of amount of fields

                        field.setAccessible(true); // this part allows to get access to private fields
                        valueOfField = field.get(tmpObject); // get value of field in object

                        if (this.name.equals("Курс")) {

                            if (field.getType().getName().contains("java.util.Set"))
                                setUtilSetTypeField(field, detectedForeignKeys);

                        } else {

                            if (valueOfField != null) {
                                strValue = valueOfField.toString();

                                if (field.getName().equals("id")) {
                                    rowDataList.add(new TableField(i, strValue, true, false, -1, null, null, null, null));
                                } else {

                                    if (!strValue.contains("server.entity.")) {
                                        if (strValue.equals("")) {
                                            rowDataList.add(new TableField(i, "/* не заполнено */", false, false, -1, null, null, null, null));
                                        } else {
                                            rowDataList.add(new TableField(i, strValue, false, false, -1, null, null, null, null)); // add value of field in list for row
                                        }
                                    } else {
                                        setForeignKeyField(strValue, valueOfField, rowDataList, detectedForeignKeys, i, k);
                                    }
                                }
                            } else {
                                if (valueOfField == null && field.getType().getName().contains("server.entity.")) {
                                    String strType = field.getType().getName();
                                    strType = strType.substring(strType.lastIndexOf('.') + 1, strType.length());
                                    rowDataList.add(
                                            new TableField(
                                                    i, "/* не заполнено */", false, true,
                                                    -1, "/* не заполнено */",
                                                    strType, detectedForeignKeys.get(strType),
                                                    valueOfField)
                                    );
                                    this.foreignKeyFieldsNumbers.add(k);
                                } else {
                                    rowDataList.add(new TableField(i, "/* не заполнено */", false, false, -1, null, null, null, null)); // add value of field in list for row
                                }
                            }
                        }
                    }
                    k++;
                }
                this.rows.put(i, rowDataList); // add rowDataList to map of table
                k = 0;
            }
    }

    void setUtilSetTypeField(Field field, Map<String, List> detectedForeignKeys) {

        field.setAccessible(true);
        String entityName;
        entityName = field.getAnnotatedType().getType().getTypeName();
        entityName = entityName.substring(entityName.lastIndexOf('.') + 1, entityName.indexOf('>'));
    }

    void setForeignKeyField(String strValue, Object valueOfField, List<TableField> rowDataList, Map<String, List> detectedForeignKeys, int i, int k) throws IllegalAccessException {

        Integer idValueOfForeignKey;
        String strValueOfForeignKey;

        strValue = strValue.substring(strValue.lastIndexOf('.') + 1, strValue.indexOf('@')); // for example: Housing
        strValueOfForeignKey = getValueOfForeignKey(valueOfField);
        idValueOfForeignKey = getIdOfForeignKey(valueOfField);

        rowDataList.add(
                new TableField(
                        i, strValueOfForeignKey, false, true,
                        idValueOfForeignKey, strValueOfForeignKey,
                        strValue, detectedForeignKeys.get(strValue),
                        valueOfField)
        );
        if (!this.foreignKeyFieldsNumbers.contains(k)) {
            this.foreignKeyFieldsNumbers.add(k);
        }
    }

    Integer getIdOfForeignKey(Object tmpObject) throws IllegalAccessException {

        Integer id;
        Integer amountOfWords = 0;

        Field[] fields = tmpObject.getClass().getDeclaredFields(); // fields of this class
        fields[0].setAccessible(true);
        id = fields[0].getInt(tmpObject);

        return id;
    } // Integer getIdOfForeignKey(Object tmpObject);

    String getValueOfForeignKey(Object tmpObject) throws IllegalAccessException {

        String strValue = "";
        Integer amountOfWords = 0;

        Field[] fields = tmpObject.getClass().getDeclaredFields(); // fields of this class

        for (Field field : fields) { // loop for fields in object

            if (amountOfWords < 2) {

                field.setAccessible(true); // this part allows to get access to private fields

                if (field.getName().toString().equals("title")) {
                    if (field.get(tmpObject) != null) {
                        strValue += field.get(tmpObject).toString() + " ";
                        amountOfWords++;
                    }
                } else {
                    if (field.getType().getName().equals("java.lang.String")) {
                        if (field.get(tmpObject) != null) {
                            amountOfWords++;
                            if (amountOfWords != 2) {
                                strValue += field.get(tmpObject).toString() + ", ";
                            } else {
                                strValue += field.get(tmpObject).toString();
                            }
                        }
                    }
                }
            }

            if(field.getName().toString().equals("surname")) {
                field.setAccessible(true);
                strValue += ", " + field.get(tmpObject).toString() + " ";
            }
        }

        if (amountOfWords < 2 && !strValue.equals("")) {
            strValue = strValue.substring(0, strValue.length()-1);
        }

        if (strValue.equals("")) {
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getType().getName().contains("int")) {
                    strValue += field.get(tmpObject).toString() + ", ";
                }
                if (field.getType().getName().contains("server.entity.")) {
                    strValue += getValueOfForeignKey(field.get(tmpObject)) + ", ";
                }
            }
            strValue = strValue.substring(0, strValue.length()-2);
        }

        return strValue;
    } // String getValueOfForeignKey(Object tmpObject);

    ForeignKey getForeignKeyObjectById(List<ForeignKey> foreignKeyList, int reqId) {

        ForeignKey requiredForeignKey = null;

        for (int i = 0; i < foreignKeyList.size(); i++) {
            if (foreignKeyList.get(i).id == reqId) {
                requiredForeignKey = foreignKeyList.get(i);
                break;
            }
        }

        return requiredForeignKey;
    }

    public Object getUpdatedRowObject(int id, List<String> updatedFieldsList) throws IllegalAccessException {

        Object objectForUpdate = null; // temporary
        Integer rowIdForUpdate = -1;

        for (int i = 0; i < this.entityList.size(); i++) { // search of Entity Object
            Object entityObject = this.entityList.get(i);
            Field[] fields = entityObject.getClass().getDeclaredFields(); // fields of this class
            fields[0].setAccessible(true);
            if (fields[0].getInt(entityObject) == id) {
                objectForUpdate = entityObject;
                break;
            }
        }

        for (int j = 0; j < this.rows.size(); j++) { // search of row in table
            if ( Integer.parseInt(this.rows.get(j).get(0).value) == id) {
                rowIdForUpdate = j;
                break;
            }
        }

        Field fieldsForUpdate[] = objectForUpdate.getClass().getDeclaredFields();
        Object valueOfField;
        Integer newIdOfForeignKey;
        Integer newId;

        Integer amountOfFields = updatedFieldsList.size();
        int k = 0;

        for (Field field : fieldsForUpdate) { // loop for fields in object

            if (k < amountOfFields) {

                field.setAccessible(true); // this part allows to get access to private fields
                TableField tableField = this.rows.get(rowIdForUpdate).get(k);

                if (!tableField.isPrimaryKey) {

                    if (tableField.isForeignKey) {
                        if (updatedFieldsList.get(k).contains("/* не заполнено */") || updatedFieldsList.get(k).equals("")) {
                            field.set(objectForUpdate, null);
                        } else {
                            newIdOfForeignKey = Integer.parseInt(updatedFieldsList.get(k));

                            if (tableField.foreignKey.id != newIdOfForeignKey) { // if something was changed
                                ForeignKey newForeignKey =
                                        getForeignKeyObjectById(tableField.variantsList, newIdOfForeignKey);
                                field.set(objectForUpdate, newForeignKey.tmpObject);

                                tableField.value = getValueOfForeignKey(newForeignKey.tmpObject);
                                tableField.foreignKey = newForeignKey;
                            }
                        }
                    } else {
                        if ( ! tableField.value.equals( updatedFieldsList.get(k) ) ) { // if something was changed

                            if (updatedFieldsList.get(k).equals("/* не заполнено */")) {
                                field.set(objectForUpdate, null);
                            } else {
                                if (field.getType().getName().equals("int")) {
                                    Integer updatedIntegerField = Integer.parseInt(updatedFieldsList.get(k));
                                    field.set(objectForUpdate, updatedIntegerField);
                                } else {
                                    field.set(objectForUpdate, updatedFieldsList.get(k));
                                }
                            }
                        }
                    }
                }

            }
            k++;
        }

        return objectForUpdate;

    }

    public Object addNewRowObject(List<String> updatedFieldsList) throws IllegalAccessException {

        Object objectForUpdate = this.entityList.get(this.entityList.size()-1); // temporary
        Integer rowIdForUpdate = this.rows.size()-1;

        Field fieldsForUpdate[] = objectForUpdate.getClass().getDeclaredFields();
        Object valueOfField;
        Integer newIdOfForeignKey;
        Integer newId;

        Integer amountOfFields = updatedFieldsList.size();
        int k = 0;

        for (Field field : fieldsForUpdate) { // loop for fields in object

            if (k < amountOfFields) {

                field.setAccessible(true); // this part allows to get access to private fields
                TableField tableField = this.rows.get(rowIdForUpdate).get(k);

                if (!tableField.isPrimaryKey) {

                    if (field.getType().getName().contains("server.entity.")) { // foreign key check

                        if (updatedFieldsList.get(k).contains("/* не заполнено */") || updatedFieldsList.get(k).equals("")) {
                            field.set(objectForUpdate, null);
                        } else {
                            newIdOfForeignKey = Integer.parseInt(updatedFieldsList.get(k));

                            tableField.isForeignKey = true;
                            tableField.variantsList = this.rows.get(0).get(k).variantsList;
                            ForeignKey newForeignKey =
                                    getForeignKeyObjectById(tableField.variantsList, newIdOfForeignKey);
                            field.set(objectForUpdate, newForeignKey.tmpObject);

                            tableField.value = getValueOfForeignKey(newForeignKey.tmpObject);
                            tableField.foreignKey = newForeignKey;
                        }
                    } else {
                        if (updatedFieldsList.get(k).equals("/* не заполнено */") || updatedFieldsList.get(k).equals("")) {
                            field.set(objectForUpdate, null);
                        } else {
                            if (field.getType().getName().equals("int")) {
                                Integer newIntegerField = Integer.parseInt(updatedFieldsList.get(k));
                                field.set(objectForUpdate, newIntegerField);
                            } else {
                                field.set(objectForUpdate, updatedFieldsList.get(k));
                            }
                        }
                    }
                } else {
                    newId = Integer.parseInt(updatedFieldsList.get(k));
                    tableField.value = updatedFieldsList.get(k);
                    field.setInt(objectForUpdate, newId);
                }

            }
            k++;
        }

        return objectForUpdate;

    }

    public List<TableField> findRowById(Integer id) {
        List<TableField> row = new ArrayList<TableField>();
        for (int i = 0; i < this.rows.size()-1; i++) { // search of Entity Object
            row = this.rows.get(i);
            if (Integer.parseInt(row.get(0).value) == id) {
                break;
            }
        }
        return row;
    }

    public void searchPhraseInEntityTable(String phraseForSearch) {

        List<TableField> tmpList;
        List<Integer> TableFieldNumbers;
        String valueOfTableField;

        this.searchOutRowsNumbers = new ArrayList<Integer>();
        this.searchOutTableFieldsNumbers = new HashMap<Integer, List<Integer>>();
        this.searchInstancesNumber = 0;

        for (int i=0; i < this.rows.size()-1; i++) {
            tmpList = rows.get(i);
            TableFieldNumbers = new ArrayList<Integer>();
            for (int k=0; k < tmpList.size(); k++) {
                if (tmpList.get(k).value != null) {
                    valueOfTableField = tmpList.get(k).value;

                    if (valueOfTableField.contains(phraseForSearch) && !valueOfTableField.equals("/* не заполнено */")) {
                        if ( !this.searchOutRowsNumbers.contains(i) ) {
                            this.searchOutRowsNumbers.add(i);
                        }
                        TableFieldNumbers.add(k);
                        this.searchInstancesNumber++;
                    } //if
                } // if
            } // for
            if (TableFieldNumbers.size() != 0) {
                this.searchOutTableFieldsNumbers.put(i, TableFieldNumbers);
            }
        } // for
    }

}

