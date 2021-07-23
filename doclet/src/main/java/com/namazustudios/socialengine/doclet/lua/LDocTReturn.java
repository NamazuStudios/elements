package com.namazustudios.socialengine.doclet.lua;

public class LDocTReturn {

    private String type;

    private String comment;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocTReturn{");
        sb.append("type='").append(type).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
