package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.AdCampaign;
import play.data.Form;
import play.mvc.*;

import views.html.*;

import java.util.Date;
import java.util.Optional;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result createAdCampaign() {
        try {
            
            Form<AdCampaign> form = Form.form(AdCampaign.class).bindFromRequest();
            if (form.hasErrors()) {
                return badRequest("Request body data doesn't conform to expected format.");
            }
            AdCampaign adCampaign = form.get();
            if (findExistingActiveCampaign(adCampaign.getPartner_id()).isPresent()) {
                return badRequest("Only one active campaign can exist for a given partner.");
            }
            adCampaign.setCreated_on(new Date());
            // store serialized ad campaign in session
            Gson gson = new GsonBuilder().create();
            session().put(adCampaign.getPartner_id(), gson.toJson(adCampaign));
        } catch (Exception e) {
            play.Logger.error(e.getMessage());  
            return internalServerError(); 
        }
        return ok("You have successfully created an ad campaign.");
    }

    public Result getAdCampaignByPartnerId(String partnerId) {
        try {
            Optional<AdCampaign> adCampaignOptional = findExistingActiveCampaign(partnerId);
            if (adCampaignOptional.isPresent()) { 
                Gson gson = new GsonBuilder().create();
                return ok(gson.toJson(adCampaignOptional.get()));
            } else {
                return badRequest("No active ad campaigns exist for the specified partner.");
            }
        } catch (Exception e) {
            play.Logger.error(e.getMessage());
            return internalServerError();
        }

    }



    /**
     * Find an active campaign for a given partner, if any.
     * @param partnerId
     * @return existing active AdCampaign if any. Otherwise return Optional Empty.
     * @throws Exception
     */
    private Optional<AdCampaign> findExistingActiveCampaign(String partnerId) throws Exception{
        String serializedExistingAdCampaign = session(partnerId);
        if (serializedExistingAdCampaign == null) {
            return Optional.empty();  
        }
        Gson gson = new GsonBuilder().create();
        AdCampaign existingAdCampaign = gson.fromJson(serializedExistingAdCampaign, AdCampaign.class); 
        Long expirationDateInSeconds = existingAdCampaign.getCreated_on().getTime()
                + existingAdCampaign.getDuration() * 1000; 
        if (new Date().getTime() < expirationDateInSeconds) {
            return Optional.of(existingAdCampaign);  
        } else {
            return Optional.empty();
        }
    }

}

