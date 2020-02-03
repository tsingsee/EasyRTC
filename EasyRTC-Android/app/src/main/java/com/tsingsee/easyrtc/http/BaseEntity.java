package com.tsingsee.easyrtc.http;

import com.google.gson.annotations.SerializedName;

/**
 * 服务器通用返回数据格式
 */
public class BaseEntity<E> {

    @SerializedName("EasyDSS")
    private EasyDarwin<E> easyDarwin;

    public EasyDarwin<E> getEasyDarwin() {
        return easyDarwin;
    }

    public void setEasyDarwin(EasyDarwin<E> easyDarwin) {
        this.easyDarwin = easyDarwin;
    }

    /**
     * EasyDarwin
     * */
    public class EasyDarwin<E> {

        @SerializedName("Header")
        private Header header;

        @SerializedName("Body")
        private E body;

        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        public E getBody() {
            return body;
        }

        public void setBody(E body) {
            this.body = body;
        }

        /**
         * Header
         * */
        public class Header {
            @SerializedName("Build")
            private String build;
            @SerializedName("Copyright")
            private String copyright;
            @SerializedName("Version")
            private String version;

//            /**
//             * 返回正确的状态码是200
//             * */
//            public boolean isSuccess() {
//                return code == 200;
//            }

            public String getBuild() {
                return build;
            }

            public void setBuild(String build) {
                this.build = build;
            }

            public String getCopyright() {
                return copyright;
            }

            public void setCopyright(String copyright) {
                this.copyright = copyright;
            }

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }
        }
    }
}
