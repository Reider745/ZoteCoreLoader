package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

import com.zhekasmirnov.innercore.api.runtime.Callback;

public class CallbackProfilingCommand extends Command {

	public CallbackProfilingCommand() {
		super("profilecallback", "Logging callback execution profiling.",
				"/profilecallback [enabled] [showParameters]");
	}

	@Override
	public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
		if (!commandSender.isOp())
			return false;

		boolean enabled = false, showParameters = false;
		if (args.length == 0) {
			enabled = !Callback.profilingEnabled;
		} else {
			enabled = "true".equalsIgnoreCase(args[0]);
			if (args.length > 1) {
				showParameters = "true".equalsIgnoreCase(args[1]);
				if (Callback.profilingShowParameters != (Callback.profilingShowParameters = showParameters)) {
					commandSender.sendMessage(
							TextFormat.YELLOW + "Parameter logging " + (showParameters ? "enabled" : "disabled") + ".");
				}
			}
		}
		if (Callback.profilingEnabled != (Callback.profilingEnabled = enabled)) {
			commandSender
					.sendMessage(TextFormat.YELLOW + "Callback profiling " + (enabled ? "enabled" : "disabled") + ".");
			if (!enabled && !showParameters)
				Callback.profilingShowParameters = false;
		}

		return true;
	}
}
