
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.lvmama.vst.order.webservice.contract package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetMsgCodeResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "getMsgCodeResponse");
    private final static QName _GetSignCreate_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "getSignCreate");
    private final static QName _GetSignCreateResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "getSignCreateResponse");
    private final static QName _UploadContract_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "uploadContract");
    private final static QName _CancelContract_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "cancelContract");
    private final static QName _SubmitStatus_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "submitStatus");
    private final static QName _SubmitStatusResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "submitStatusResponse");
    private final static QName _ResendMsgResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "resendMsgResponse");
    private final static QName _ComplementGuest_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "complementGuest");
    private final static QName _ComplementInsurance_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "complementInsurance");
    private final static QName _SubmitSignResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "submitSignResponse");
    private final static QName _AuthenticationResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "authenticationResponse");
    private final static QName _Authentication_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "authentication");
    private final static QName _CancelContractResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "cancelContractResponse");
    private final static QName _SubmitContractResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "submitContractResponse");
    private final static QName _ComplementInsuranceResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "complementInsuranceResponse");
    private final static QName _SubmitContract_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "submitContract");
    private final static QName _GetMsgCode_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "getMsgCode");
    private final static QName _GetContractUuid_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "getContractUuid");
    private final static QName _ResendMsg_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "resendMsg");
    private final static QName _GetContractUuidResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "getContractUuidResponse");
    private final static QName _UploadContractResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "uploadContractResponse");
    private final static QName _ComplementGuestResponse_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "complementGuestResponse");
    private final static QName _SubmitSign_QNAME = new QName("http://server.ws.api.contract.goldpalm.com/", "submitSign");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.lvmama.vst.order.webservice.contract
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ComplementSignRequest }
     * 
     */
    public ComplementSignRequest createComplementSignRequest() {
        return new ComplementSignRequest();
    }

    /**
     * Create an instance of {@link ComplementGuest }
     * 
     */
    public ComplementGuest createComplementGuest() {
        return new ComplementGuest();
    }

    /**
     * Create an instance of {@link SubmitContractRequest }
     * 
     */
    public SubmitContractRequest createSubmitContractRequest() {
        return new SubmitContractRequest();
    }

    /**
     * Create an instance of {@link GetMsgCode }
     * 
     */
    public GetMsgCode createGetMsgCode() {
        return new GetMsgCode();
    }

    /**
     * Create an instance of {@link ComplementInsurance }
     * 
     */
    public ComplementInsurance createComplementInsurance() {
        return new ComplementInsurance();
    }

    /**
     * Create an instance of {@link GetSignCreate }
     * 
     */
    public GetSignCreate createGetSignCreate() {
        return new GetSignCreate();
    }

    /**
     * Create an instance of {@link SubmitStatus }
     * 
     */
    public SubmitStatus createSubmitStatus() {
        return new SubmitStatus();
    }

    /**
     * Create an instance of {@link SubmitSignResponse }
     * 
     */
    public SubmitSignResponse createSubmitSignResponse() {
        return new SubmitSignResponse();
    }

    /**
     * Create an instance of {@link SubmitContractResponse }
     * 
     */
    public SubmitContractResponse createSubmitContractResponse() {
        return new SubmitContractResponse();
    }

    /**
     * Create an instance of {@link CancelContractRequest }
     * 
     */
    public CancelContractRequest createCancelContractRequest() {
        return new CancelContractRequest();
    }

    /**
     * Create an instance of {@link ComplementInsuranceResponse }
     * 
     */
    public ComplementInsuranceResponse createComplementInsuranceResponse() {
        return new ComplementInsuranceResponse();
    }

    /**
     * Create an instance of {@link SubmitStatusRequest }
     * 
     */
    public SubmitStatusRequest createSubmitStatusRequest() {
        return new SubmitStatusRequest();
    }

    /**
     * Create an instance of {@link ContractTeamGuest }
     * 
     */
    public ContractTeamGuest createContractTeamGuest() {
        return new ContractTeamGuest();
    }

    /**
     * Create an instance of {@link ContractTeamRoute }
     * 
     */
    public ContractTeamRoute createContractTeamRoute() {
        return new ContractTeamRoute();
    }

    /**
     * Create an instance of {@link Authentication }
     * 
     */
    public Authentication createAuthentication() {
        return new Authentication();
    }

    /**
     * Create an instance of {@link ResendMsg }
     * 
     */
    public ResendMsg createResendMsg() {
        return new ResendMsg();
    }

    /**
     * Create an instance of {@link ContractJSON }
     * 
     */
    public ContractJSON createContractJSON() {
        return new ContractJSON();
    }

    /**
     * Create an instance of {@link CancelContractResponse }
     * 
     */
    public CancelContractResponse createCancelContractResponse() {
        return new CancelContractResponse();
    }

    /**
     * Create an instance of {@link GetContractUuid }
     * 
     */
    public GetContractUuid createGetContractUuid() {
        return new GetContractUuid();
    }

    /**
     * Create an instance of {@link GetMsgCodeResponse }
     * 
     */
    public GetMsgCodeResponse createGetMsgCodeResponse() {
        return new GetMsgCodeResponse();
    }

    /**
     * Create an instance of {@link CancelContract }
     * 
     */
    public CancelContract createCancelContract() {
        return new CancelContract();
    }

    /**
     * Create an instance of {@link ComplementGuestResponse }
     * 
     */
    public ComplementGuestResponse createComplementGuestResponse() {
        return new ComplementGuestResponse();
    }

    /**
     * Create an instance of {@link ComplementInsuranceRequest }
     * 
     */
    public ComplementInsuranceRequest createComplementInsuranceRequest() {
        return new ComplementInsuranceRequest();
    }

    /**
     * Create an instance of {@link ContractTeamActivity }
     * 
     */
    public ContractTeamActivity createContractTeamActivity() {
        return new ContractTeamActivity();
    }

    /**
     * Create an instance of {@link ComplementGuestRequest }
     * 
     */
    public ComplementGuestRequest createComplementGuestRequest() {
        return new ComplementGuestRequest();
    }

    /**
     * Create an instance of {@link ResendMsgResponse }
     * 
     */
    public ResendMsgResponse createResendMsgResponse() {
        return new ResendMsgResponse();
    }

    /**
     * Create an instance of {@link SubmitSign }
     * 
     */
    public SubmitSign createSubmitSign() {
        return new SubmitSign();
    }

    /**
     * Create an instance of {@link GetContractUuidResponse }
     * 
     */
    public GetContractUuidResponse createGetContractUuidResponse() {
        return new GetContractUuidResponse();
    }

    /**
     * Create an instance of {@link ContractTeam }
     * 
     */
    public ContractTeam createContractTeam() {
        return new ContractTeam();
    }

    /**
     * Create an instance of {@link GetSignCreateResponse }
     * 
     */
    public GetSignCreateResponse createGetSignCreateResponse() {
        return new GetSignCreateResponse();
    }

    /**
     * Create an instance of {@link UploadContractResponse }
     * 
     */
    public UploadContractResponse createUploadContractResponse() {
        return new UploadContractResponse();
    }

    /**
     * Create an instance of {@link ContractTeamShopping }
     * 
     */
    public ContractTeamShopping createContractTeamShopping() {
        return new ContractTeamShopping();
    }

    /**
     * Create an instance of {@link SubmitStatusResponse }
     * 
     */
    public SubmitStatusResponse createSubmitStatusResponse() {
        return new SubmitStatusResponse();
    }

    /**
     * Create an instance of {@link SubmitContract }
     * 
     */
    public SubmitContract createSubmitContract() {
        return new SubmitContract();
    }

    /**
     * Create an instance of {@link UploadContract }
     * 
     */
    public UploadContract createUploadContract() {
        return new UploadContract();
    }

    /**
     * Create an instance of {@link AuthenticationResponse }
     * 
     */
    public AuthenticationResponse createAuthenticationResponse() {
        return new AuthenticationResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMsgCodeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "getMsgCodeResponse")
    public JAXBElement<GetMsgCodeResponse> createGetMsgCodeResponse(GetMsgCodeResponse value) {
        return new JAXBElement<GetMsgCodeResponse>(_GetMsgCodeResponse_QNAME, GetMsgCodeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSignCreate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "getSignCreate")
    public JAXBElement<GetSignCreate> createGetSignCreate(GetSignCreate value) {
        return new JAXBElement<GetSignCreate>(_GetSignCreate_QNAME, GetSignCreate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSignCreateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "getSignCreateResponse")
    public JAXBElement<GetSignCreateResponse> createGetSignCreateResponse(GetSignCreateResponse value) {
        return new JAXBElement<GetSignCreateResponse>(_GetSignCreateResponse_QNAME, GetSignCreateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadContract }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "uploadContract")
    public JAXBElement<UploadContract> createUploadContract(UploadContract value) {
        return new JAXBElement<UploadContract>(_UploadContract_QNAME, UploadContract.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CancelContract }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "cancelContract")
    public JAXBElement<CancelContract> createCancelContract(CancelContract value) {
        return new JAXBElement<CancelContract>(_CancelContract_QNAME, CancelContract.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "submitStatus")
    public JAXBElement<SubmitStatus> createSubmitStatus(SubmitStatus value) {
        return new JAXBElement<SubmitStatus>(_SubmitStatus_QNAME, SubmitStatus.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "submitStatusResponse")
    public JAXBElement<SubmitStatusResponse> createSubmitStatusResponse(SubmitStatusResponse value) {
        return new JAXBElement<SubmitStatusResponse>(_SubmitStatusResponse_QNAME, SubmitStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResendMsgResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "resendMsgResponse")
    public JAXBElement<ResendMsgResponse> createResendMsgResponse(ResendMsgResponse value) {
        return new JAXBElement<ResendMsgResponse>(_ResendMsgResponse_QNAME, ResendMsgResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ComplementGuest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "complementGuest")
    public JAXBElement<ComplementGuest> createComplementGuest(ComplementGuest value) {
        return new JAXBElement<ComplementGuest>(_ComplementGuest_QNAME, ComplementGuest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ComplementInsurance }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "complementInsurance")
    public JAXBElement<ComplementInsurance> createComplementInsurance(ComplementInsurance value) {
        return new JAXBElement<ComplementInsurance>(_ComplementInsurance_QNAME, ComplementInsurance.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitSignResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "submitSignResponse")
    public JAXBElement<SubmitSignResponse> createSubmitSignResponse(SubmitSignResponse value) {
        return new JAXBElement<SubmitSignResponse>(_SubmitSignResponse_QNAME, SubmitSignResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthenticationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "authenticationResponse")
    public JAXBElement<AuthenticationResponse> createAuthenticationResponse(AuthenticationResponse value) {
        return new JAXBElement<AuthenticationResponse>(_AuthenticationResponse_QNAME, AuthenticationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Authentication }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "authentication")
    public JAXBElement<Authentication> createAuthentication(Authentication value) {
        return new JAXBElement<Authentication>(_Authentication_QNAME, Authentication.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CancelContractResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "cancelContractResponse")
    public JAXBElement<CancelContractResponse> createCancelContractResponse(CancelContractResponse value) {
        return new JAXBElement<CancelContractResponse>(_CancelContractResponse_QNAME, CancelContractResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitContractResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "submitContractResponse")
    public JAXBElement<SubmitContractResponse> createSubmitContractResponse(SubmitContractResponse value) {
        return new JAXBElement<SubmitContractResponse>(_SubmitContractResponse_QNAME, SubmitContractResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ComplementInsuranceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "complementInsuranceResponse")
    public JAXBElement<ComplementInsuranceResponse> createComplementInsuranceResponse(ComplementInsuranceResponse value) {
        return new JAXBElement<ComplementInsuranceResponse>(_ComplementInsuranceResponse_QNAME, ComplementInsuranceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitContract }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "submitContract")
    public JAXBElement<SubmitContract> createSubmitContract(SubmitContract value) {
        return new JAXBElement<SubmitContract>(_SubmitContract_QNAME, SubmitContract.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMsgCode }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "getMsgCode")
    public JAXBElement<GetMsgCode> createGetMsgCode(GetMsgCode value) {
        return new JAXBElement<GetMsgCode>(_GetMsgCode_QNAME, GetMsgCode.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetContractUuid }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "getContractUuid")
    public JAXBElement<GetContractUuid> createGetContractUuid(GetContractUuid value) {
        return new JAXBElement<GetContractUuid>(_GetContractUuid_QNAME, GetContractUuid.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResendMsg }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "resendMsg")
    public JAXBElement<ResendMsg> createResendMsg(ResendMsg value) {
        return new JAXBElement<ResendMsg>(_ResendMsg_QNAME, ResendMsg.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetContractUuidResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "getContractUuidResponse")
    public JAXBElement<GetContractUuidResponse> createGetContractUuidResponse(GetContractUuidResponse value) {
        return new JAXBElement<GetContractUuidResponse>(_GetContractUuidResponse_QNAME, GetContractUuidResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadContractResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "uploadContractResponse")
    public JAXBElement<UploadContractResponse> createUploadContractResponse(UploadContractResponse value) {
        return new JAXBElement<UploadContractResponse>(_UploadContractResponse_QNAME, UploadContractResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ComplementGuestResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "complementGuestResponse")
    public JAXBElement<ComplementGuestResponse> createComplementGuestResponse(ComplementGuestResponse value) {
        return new JAXBElement<ComplementGuestResponse>(_ComplementGuestResponse_QNAME, ComplementGuestResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitSign }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.ws.api.contract.goldpalm.com/", name = "submitSign")
    public JAXBElement<SubmitSign> createSubmitSign(SubmitSign value) {
        return new JAXBElement<SubmitSign>(_SubmitSign_QNAME, SubmitSign.class, null, value);
    }

}
