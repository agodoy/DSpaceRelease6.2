/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.curp;

import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueServiceImpl;
import org.dspace.authority.PersonAuthorityValue;
import org.dspace.authority.orcid.Orcid;
import org.dspace.authority.orcid.model.Bio;
import org.dspace.authority.orcid.model.BioExternalIdentifier;
import org.dspace.authority.orcid.model.BioName;
import org.dspace.authority.orcid.model.BioResearcherUrl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.*;

public class CurpAuthorityValue extends PersonAuthorityValue {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(CurpAuthorityValue.class);

    private String curp_id;
    private Map<String, List<String>> otherMetadata = new HashMap<String, List<String>>();
    private boolean update; // used in setValues(Bio bio)


    /**
     * Creates an instance of OrcidAuthorityValue with only uninitialized fields.
     * This is meant to be filled in with values from an existing record.
     * To create a brand new OrcidAuthorityValue, use create()
     */
    public CurpAuthorityValue() {
    }

    public CurpAuthorityValue(SolrDocument document) {
        super(document);
    }

    public String getCurp_id() {
		return curp_id;
	}

	public void setCurp_id(String curp_id) {
		this.curp_id = curp_id;
	}

	public Map<String, List<String>> getOtherMetadata() {
        return otherMetadata;
    }

    public void addOtherMetadata(String label, String data) {
        List<String> strings = otherMetadata.get(label);
        if (strings == null) {
            strings = new ArrayList<String>();
        }
        strings.add(data);
        otherMetadata.put(label, strings);
    }

    @Override
    public SolrInputDocument getSolrInputDocument() {
        SolrInputDocument doc = super.getSolrInputDocument();
        if (StringUtils.isNotBlank(getCurp_id())) {
            doc.addField("curp_id", getCurp_id());
        }

        for (String t : otherMetadata.keySet()) {
            List<String> data = otherMetadata.get(t);
            for (String data_entry : data) {
                doc.addField("label_" + t, data_entry);
            }
        }
        return doc;
    }

    @Override
    public void setValues(SolrDocument document) {
        super.setValues(document);
        this.curp_id = String.valueOf(document.getFieldValue("curp_id"));

        otherMetadata = new HashMap<String, List<String>>();
        for (String fieldName : document.getFieldNames()) {
            String labelPrefix = "label_";
            if (fieldName.startsWith(labelPrefix)) {
                String label = fieldName.substring(labelPrefix.length());
                List<String> list = new ArrayList<String>();
                Collection<Object> fieldValues = document.getFieldValues(fieldName);
                for (Object o : fieldValues) {
                    list.add(String.valueOf(o));
                }
                otherMetadata.put(label, list);
            }
        }
    }

    public static CurpAuthorityValue create() {
        CurpAuthorityValue orcidAuthorityValue = new CurpAuthorityValue();
        orcidAuthorityValue.setId(UUID.randomUUID().toString());
        orcidAuthorityValue.updateLastModifiedDate();
        orcidAuthorityValue.setCreationDate(new Date());
        return orcidAuthorityValue;
    }

    /**
     * Create an authority based on a given orcid bio
     * @param bio Bio
     * @return OrcidAuthorityValue
     */
    public static CurpAuthorityValue create(Bio bio) {
        CurpAuthorityValue authority = CurpAuthorityValue.create();

        authority.setValues(bio);

        return authority;
    }

    public boolean setValues(Bio bio) {
        BioName name = bio.getName();

        if (updateValue(bio.getOrcid(), getCurp_id())) {
            setCurp_id(bio.getOrcid());
        }

        if (updateValue(name.getFamilyName(), getLastName())) {
            setLastName(name.getFamilyName());
        }

        if (updateValue(name.getGivenNames(), getFirstName())) {
            setFirstName(name.getGivenNames());
        }

        if (StringUtils.isNotBlank(name.getCreditName())) {
            if (!getNameVariants().contains(name.getCreditName())) {
                addNameVariant(name.getCreditName());
                update = true;
            }
        }
        for (String otherName : name.getOtherNames()) {
            if (!getNameVariants().contains(otherName)) {
                addNameVariant(otherName);
                update = true;
            }
        }

        if (updateOtherMetadata("country", bio.getCountry())) {
            addOtherMetadata("country", bio.getCountry());
        }

        for (String keyword : bio.getKeywords()) {
            if (updateOtherMetadata("keyword", keyword)) {
                addOtherMetadata("keyword", keyword);
            }
        }

        for (BioExternalIdentifier externalIdentifier : bio.getBioExternalIdentifiers()) {
            if (updateOtherMetadata("external_identifier", externalIdentifier.toString())) {
                addOtherMetadata("external_identifier", externalIdentifier.toString());
            }
        }

        for (BioResearcherUrl researcherUrl : bio.getResearcherUrls()) {
            if (updateOtherMetadata("researcher_url", researcherUrl.toString())) {
                addOtherMetadata("researcher_url", researcherUrl.toString());
            }
        }

        if (updateOtherMetadata("biography", bio.getBiography())) {
            addOtherMetadata("biography", bio.getBiography());
        }

        setValue(getName());

        if (update) {
            update();
        }
        boolean result = update;
        update = false;
        return result;
    }

    private boolean updateOtherMetadata(String label, String data) {
        List<String> strings = getOtherMetadata().get(label);
        boolean update;
        if (strings == null) {
            update = StringUtils.isNotBlank(data);
        } else {
            update = !strings.contains(data);
        }
        if (update) {
            this.update = true;
        }
        return update;
    }

    private boolean updateValue(String incoming, String resident) {
        boolean update = StringUtils.isNotBlank(incoming) && !incoming.equals(resident);
        if (update) {
            this.update = true;
        }
        return update;
    }

    @Override
    public Map<String, String> choiceSelectMap() {

        Map<String, String> map = super.choiceSelectMap();

        map.put("curpid", getCurp_id());

        return map;
    }

    @Override
    public String getAuthorityType() {
        return "curp";
    }

    @Override
    public String generateString() {
        String generateString = AuthorityValueServiceImpl.GENERATE + getAuthorityType() + AuthorityValueServiceImpl.SPLIT;
        if (StringUtils.isNotBlank(getCurp_id())) {
            generateString += getCurp_id();
        }
        return generateString;
    }


    @Override
    public AuthorityValue newInstance(String info) {
        AuthorityValue authorityValue = null;
        if (StringUtils.isNotBlank(info)) {
            Orcid orcid = Orcid.getOrcid();
            authorityValue = orcid.queryAuthorityID(info);
        } else {
            authorityValue = CurpAuthorityValue.create();
        }
        return authorityValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CurpAuthorityValue that = (CurpAuthorityValue) o;

        if (curp_id != null ? !curp_id.equals(that.curp_id) : that.curp_id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return curp_id != null ? curp_id.hashCode() : 0;
    }

    @Override
    public boolean hasTheSameInformationAs(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.hasTheSameInformationAs(o)) {
            return false;
        }

        CurpAuthorityValue that = (CurpAuthorityValue) o;

        if (curp_id != null ? !curp_id.equals(that.curp_id) : that.curp_id != null) {
            return false;
        }

        for (String key : otherMetadata.keySet()) {
            if(otherMetadata.get(key) != null){
                List<String> metadata = otherMetadata.get(key);
                List<String> otherMetadata = that.otherMetadata.get(key);
                if (otherMetadata == null) {
                    return false;
                } else {
                    HashSet<String> metadataSet = new HashSet<String>(metadata);
                    HashSet<String> otherMetadataSet = new HashSet<String>(otherMetadata);
                    if (!metadataSet.equals(otherMetadataSet)) {
                        return false;
                    }
                }
            }else{
                if(that.otherMetadata.get(key) != null){
                    return false;
                }
            }
        }

        return true;
    }
}
